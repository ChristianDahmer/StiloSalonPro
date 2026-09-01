package com.example.data

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.isActive

object GoogleDriveBackupManager {
    private const val TAG = "GDriveBackup"
    private const val PREFS_NAME = "google_drive_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"

    private const val DEFAULT_CLIENT_ID = "466466787667-7516l0imqgfmk8p1n669rqvdh4nsoohr.apps.googleusercontent.com"
    private const val REDIRECT_URI = "http://localhost"
    
    private val client = OkHttpClient()

    data class DriveFile(
        val id: String,
        val name: String,
        val createdTime: String
    )

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getClientId(context: Context): String {
        return getPrefs(context).getString("custom_client_id", null)?.takeIf { it.isNotBlank() } ?: DEFAULT_CLIENT_ID
    }

    fun getClientSecret(context: Context): String {
        return getPrefs(context).getString("custom_client_secret", null) ?: ""
    }

    fun saveClientCredentials(context: Context, clientId: String, clientSecret: String) {
        getPrefs(context).edit()
            .putString("custom_client_id", clientId.trim())
            .putString("custom_client_secret", clientSecret.trim())
            .apply()
    }

    fun getOAuthLoginUrl(context: Context): String {
        val clientId = getClientId(context)
        val encodedRedirect = android.net.Uri.encode(REDIRECT_URI)
        val encodedScope = android.net.Uri.encode("https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/userinfo.email")
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId" +
                "&redirect_uri=$encodedRedirect" +
                "&response_type=code" +
                "&scope=$encodedScope" +
                "&prompt=consent" +
                "&access_type=offline"
    }

    fun saveDirectAccessToken(context: Context, accessToken: String) {
        val cleanToken = accessToken.trim()
        val expiryTime = System.currentTimeMillis() + (3600 * 1000)
        getPrefs(context).edit()
            .putString(KEY_ACCESS_TOKEN, cleanToken)
            .putString(KEY_REFRESH_TOKEN, cleanToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }

    fun isConnected(context: Context): Boolean {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null) != null
    }

    fun disconnect(context: Context) {
        cancelBackupAlarm(context)
        getPrefs(context).edit().clear().apply()
    }

    fun getSavedConnectedEmail(context: Context): String? {
        return getPrefs(context).getString("connected_email", null)
    }

    suspend fun fetchUserEmail(context: Context): Result<String> {
        val tokenResult = getValidAccessToken(context)
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Autenticação falhou"))
        }
        val token = tokenResult.getOrThrow()

        val request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v3/userinfo")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        return executeRequest(request) { json ->
            val email = json.optString("email", "Desconhecido")
            getPrefs(context).edit().putString("connected_email", email).apply()
            Result.success(email)
        }
    }

    // Backup scheduling methods
    fun getScheduleInterval(context: Context): String {
        return getPrefs(context).getString("schedule_interval", "disabled") ?: "disabled"
    }

    fun getScheduleHour(context: Context): Int {
        return getPrefs(context).getInt("schedule_hour", 3)
    }

    fun getScheduleMinute(context: Context): Int {
        return getPrefs(context).getInt("schedule_minute", 0)
    }

    fun saveScheduleSettings(context: Context, interval: String, hour: Int, minute: Int) {
        getPrefs(context).edit()
            .putString("schedule_interval", interval)
            .putInt("schedule_hour", hour)
            .putInt("schedule_minute", minute)
            .apply()
        setupBackupAlarm(context)
    }

    fun cancelBackupAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, com.example.service.BackupAlarmReceiver::class.java).apply {
            action = "com.example.action.PERFORM_BACKUP"
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            9991,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun setupBackupAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, com.example.service.BackupAlarmReceiver::class.java).apply {
            action = "com.example.action.PERFORM_BACKUP"
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            9991,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val interval = getScheduleInterval(context)
        if (interval == "disabled" || !isConnected(context)) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Backup alarm cancelado (desativado ou desconectado).")
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, getScheduleHour(context))
            set(Calendar.MINUTE, getScheduleMinute(context))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intervalMs = if (interval == "daily") {
            android.app.AlarmManager.INTERVAL_DAY
        } else {
            android.app.AlarmManager.INTERVAL_DAY * 7
        }

        try {
            alarmManager.setInexactRepeating(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intervalMs,
                pendingIntent
            )
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            Log.d(TAG, "Backup alarm agendado para: ${sdf.format(calendar.time)} (Intervalo: $interval)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao agendar backup alarm", e)
        }
    }

    private fun saveTokens(context: Context, accessToken: String, refreshToken: String?, expiresInSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        val edit = getPrefs(context).edit()
        edit.putString(KEY_ACCESS_TOKEN, accessToken)
        if (refreshToken != null) {
            edit.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        edit.putLong(KEY_TOKEN_EXPIRY, expiryTime)
        edit.apply()
    }

    private fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    private fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    private fun isTokenExpired(context: Context): Boolean {
        val expiry = getPrefs(context).getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() + 300000 >= expiry
    }

    private fun extractCodeInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val uri = android.net.Uri.parse(trimmed)
            val codeParam = uri.getQueryParameter("code")
            if (!codeParam.isNullOrBlank()) {
                return codeParam
            }
        }
        if (trimmed.contains("code=")) {
            val match = Regex("""code=([^&]+)""").find(trimmed)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return trimmed
    }

    suspend fun exchangeCodeForTokens(context: Context, codeOrUrlOrToken: String): Result<Unit> {
        val cleaned = codeOrUrlOrToken.trim()
        if (cleaned.startsWith("ya29.")) {
            // Direct OAuth Access Token
            saveDirectAccessToken(context, cleaned)
            return Result.success(Unit)
        }

        val code = extractCodeInput(cleaned)
        val clientId = getClientId(context)
        val clientSecret = getClientSecret(context)

        val builder = FormBody.Builder()
            .add("client_id", clientId)
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", REDIRECT_URI)
        
        if (clientSecret.isNotBlank()) {
            builder.add("client_secret", clientSecret)
        }

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(builder.build())
            .build()

        return executeRequest(request) { json ->
            val accessToken = json.getString("access_token")
            val refreshToken = json.optString("refresh_token").takeIf { it.isNotEmpty() }
            val expiresIn = json.optLong("expires_in", 3600)
            saveTokens(context, accessToken, refreshToken ?: getRefreshToken(context), expiresIn)
            Result.success(Unit)
        }
    }

    private suspend fun refreshAccessToken(context: Context): Result<String> {
        val refreshToken = getRefreshToken(context)
            ?: return Result.failure(Exception("Nenhum token de atualização (refresh token) disponível. Faça login novamente."))

        val clientId = getClientId(context)
        val clientSecret = getClientSecret(context)

        val builder = FormBody.Builder()
            .add("client_id", clientId)
            .add("refresh_token", refreshToken)
            .add("grant_type", "refresh_token")

        if (clientSecret.isNotBlank()) {
            builder.add("client_secret", clientSecret)
        }

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(builder.build())
            .build()

        return executeRequest(request) { json ->
            val accessToken = json.getString("access_token")
            val expiresIn = json.optLong("expires_in", 3600)
            saveTokens(context, accessToken, getRefreshToken(context), expiresIn)
            Result.success(accessToken)
        }
    }

    private suspend fun getValidAccessToken(context: Context): Result<String> {
        if (!isConnected(context)) {
            return Result.failure(Exception("Google Drive não está conectado."))
        }
        if (isTokenExpired(context)) {
            Log.d(TAG, "Token expirado ou prestes a expirar. Atualizando...")
            return refreshAccessToken(context)
        }
        val token = getAccessToken(context)
        return if (token != null) {
            Result.success(token)
        } else {
            refreshAccessToken(context)
        }
    }

    private suspend fun <T> executeRequest(
        request: Request,
        parser: (JSONObject) -> Result<T>
    ): Result<T> = suspendCancellableCoroutine { continuation ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!continuation.isActive) return
                val bodyStr = response.body?.string()
                if (response.isSuccessful && bodyStr != null) {
                    try {
                        val json = JSONObject(bodyStr)
                        continuation.resume(parser(json))
                    } catch (e: Exception) {
                        continuation.resume(Result.failure(e))
                    }
                } else {
                    val errMsg = try {
                        JSONObject(bodyStr ?: "").optString("error_description", "Erro de rede Google API")
                    } catch (e: Exception) {
                        "Erro na chamada do Google: Código ${response.code}"
                    }
                    continuation.resume(Result.failure(Exception(errMsg)))
                }
            }
        })
    }

    suspend fun uploadDatabaseBackup(context: Context): Result<Unit> {
        val tokenResult = getValidAccessToken(context)
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Erro de autenticação"))
        }
        val token = tokenResult.getOrThrow()

        // 1. Force full database WAL checkpoint
        try {
            val db = AppDatabase.getDatabase(context)
            val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")
            db.openHelper.writableDatabase.query(query).close()
            Log.d(TAG, "Database WAL checkpoint completo.")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao realizar WAL checkpoint", e)
        }

        // 2. Read database file
        val dbFile = context.getDatabasePath("salon_beauty_db")
        if (!dbFile.exists()) {
            return Result.failure(Exception("Arquivo de banco de dados não encontrado."))
        }
        val dbBytes = dbFile.readBytes()

        // 3. Prepare upload metadata & content
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val filename = "salon_backup_$timestamp.sqlite"

        val metadataJson = JSONObject().apply {
            put("name", filename)
            put("mimeType", "application/x-sqlite3")
        }.toString()

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                Headers.headersOf("Content-Type", "application/json; charset=UTF-8"),
                metadataJson.toRequestBody("application/json".toMediaType())
            )
            .addPart(
                Headers.headersOf("Content-Disposition", "form-data; name=\"file\""),
                dbBytes.toRequestBody("application/x-sqlite3".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            .header("Authorization", "Bearer $token")
            .post(multipartBody)
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!continuation.isActive) return
                    if (response.isSuccessful) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        val body = response.body?.string() ?: ""
                        continuation.resume(Result.failure(Exception("Falha ao subir arquivo para o Drive: ${response.code} $body")))
                    }
                }
            })
        }
    }

    suspend fun listBackups(context: Context): Result<List<DriveFile>> {
        val tokenResult = getValidAccessToken(context)
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Erro de autenticação"))
        }
        val token = tokenResult.getOrThrow()

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.googleapis.com")
            .addPathSegments("drive/v3/files")
            .addQueryParameter("q", "name contains 'salon_backup_' and trashed = false")
            .addQueryParameter("orderBy", "createdTime desc")
            .addQueryParameter("fields", "files(id, name, createdTime)")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        return executeRequest(request) { json ->
            val filesArray = json.getJSONArray("files")
            val filesList = mutableListOf<DriveFile>()
            for (i in 0 until filesArray.length()) {
                val fObj = filesArray.getJSONObject(i)
                filesList.add(
                    DriveFile(
                        id = fObj.getString("id"),
                        name = fObj.getString("name"),
                        createdTime = fObj.optString("createdTime", "")
                    )
                )
            }
            Result.success(filesList)
        }
    }

    suspend fun restoreDatabaseBackup(context: Context, fileId: String): Result<Unit> {
        val tokenResult = getValidAccessToken(context)
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Erro de autenticação"))
        }
        val token = tokenResult.getOrThrow()

        val request = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!continuation.isActive) return
                    if (response.isSuccessful && response.body != null) {
                        try {
                            // 1. Close current database
                            AppDatabase.getDatabase(context).close()

                            // 2. Delete wal and shm files
                            val walFile = context.getDatabasePath("salon_beauty_db-wal")
                            val shmFile = context.getDatabasePath("salon_beauty_db-shm")
                            if (walFile.exists()) walFile.delete()
                            if (shmFile.exists()) shmFile.delete()

                            // 3. Write downloaded bytes to database file
                            val dbFile = context.getDatabasePath("salon_beauty_db")
                            val inputStream = response.body!!.byteStream()
                            val outputStream = FileOutputStream(dbFile)
                            
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                            outputStream.flush()
                            outputStream.close()
                            inputStream.close()

                            continuation.resume(Result.success(Unit))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    } else {
                        continuation.resume(Result.failure(Exception("Erro ao baixar backup: Código ${response.code}")))
                    }
                }
            })
        }
    }

    fun triggerAppRestart(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        Runtime.getRuntime().exit(0)
    }
}
