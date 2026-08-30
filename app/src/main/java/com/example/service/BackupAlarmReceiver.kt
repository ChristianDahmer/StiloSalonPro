package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.GoogleDriveBackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BackupAlarmReceiver : BroadcastReceiver() {
    private val TAG = "BackupAlarmReceiver"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive chamado com action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Dispositivo reiniciado. Reconfigurando agendamento de backup...")
            GoogleDriveBackupManager.setupBackupAlarm(context)
            return
        }

        if (action == "com.example.action.PERFORM_BACKUP") {
            Log.d(TAG, "Iniciando backup em segundo plano agendado...")
            val pendingResult = goAsync()

            scope.launch {
                try {
                    val result = GoogleDriveBackupManager.uploadDatabaseBackup(context)
                    if (result.isSuccess) {
                        Log.d(TAG, "Backup agendado em segundo plano realizado com sucesso!")
                        showNotification(
                            context,
                            "Backup Automático Concluído",
                            "Seu salão de beleza foi salvo com sucesso no Google Drive!"
                        )
                    } else {
                        val errMsg = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                        Log.e(TAG, "Falha no backup agendado: $errMsg")
                        showNotification(
                            context,
                            "Falha no Backup Automático",
                            "Não foi possível salvar os dados no Drive: $errMsg"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exceção ao realizar backup agendado", e)
                    showNotification(
                        context,
                        "Erro no Backup Automático",
                        "Ocorreu um erro inesperado ao salvar no Drive."
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "backup_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Backup do Google Drive",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações do status de backups agendados no Google Drive."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        notificationManager.notify(991122, notification)
    }
}
