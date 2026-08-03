package com.example.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MaskUtils {

    fun unmask(text: String): String {
        return text.replace(Regex("[^0-9]"), "")
    }

    fun formatCpf(cpf: String): String {
        val digits = unmask(cpf).take(11)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 2 || i == 5) {
                if (i < digits.length - 1) sb.append(".")
            } else if (i == 8) {
                if (i < digits.length - 1) sb.append("-")
            }
        }
        return sb.toString()
    }

    fun formatCnpj(cnpj: String): String {
        val digits = unmask(cnpj).take(14)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 1) {
                if (i < digits.length - 1) sb.append(".")
            } else if (i == 4) {
                if (i < digits.length - 1) sb.append(".")
            } else if (i == 8) {
                if (i < digits.length - 1) sb.append("/")
            } else if (i == 12) {
                if (i < digits.length - 1) sb.append("-")
            }
        }
        return sb.toString()
    }

    fun formatInscricaoEstadual(ie: String): String {
        val digits = unmask(ie).take(12)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 2 || i == 5 || i == 8) {
                if (i < digits.length - 1) sb.append(".")
            }
        }
        return sb.toString()
    }

    fun formatPhone(phone: String): String {
        val digits = unmask(phone).take(11)
        val sb = StringBuilder()
        if (digits.isNotEmpty()) sb.append("(")
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 1) {
                sb.append(") ")
            } else if (digits.length == 11 && i == 6) {
                sb.append("-")
            } else if (digits.length <= 10 && i == 5) {
                sb.append("-")
            }
        }
        return sb.toString()
    }

    fun formatCurrency(value: Double): String {
        val ptBr = Locale("pt", "BR")
        val formatter = NumberFormat.getCurrencyInstance(ptBr)
        return formatter.format(value)
    }

    fun formatDatePtBr(dateStr: String): String {
        return try {
            if (dateStr.contains("-")) {
                val parts = dateStr.split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else dateStr
            } else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getCurrentDateIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDateTimeIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun isCpfValid(cpf: String): Boolean {
        val digits = unmask(cpf)
        return digits.length == 11
    }

    fun isCnpjValid(cnpj: String): Boolean {
        val digits = unmask(cnpj)
        return digits.length == 14
    }
}

class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 2 || i == 5) sb.append(".")
            else if (i == 8) sb.append("-")
        }
        val transformed = sb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceAtMost(digits.length)
                return when {
                    clamped <= 2 -> clamped
                    clamped <= 5 -> clamped + 1
                    clamped <= 8 -> clamped + 2
                    else -> (clamped + 3).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceAtMost(transformed.length)
                return when {
                    clamped <= 3 -> clamped
                    clamped <= 7 -> clamped - 1
                    clamped <= 11 -> clamped - 2
                    else -> (clamped - 3).coerceAtMost(digits.length)
                }
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class CnpjVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(14)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 1 || i == 4) sb.append(".")
            else if (i == 7) sb.append("/")
            else if (i == 11) sb.append("-")
        }
        val transformed = sb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceAtMost(digits.length)
                return when {
                    clamped <= 2 -> clamped
                    clamped <= 5 -> clamped + 1
                    clamped <= 8 -> clamped + 2
                    clamped <= 12 -> clamped + 3
                    else -> (clamped + 4).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceAtMost(transformed.length)
                return when {
                    clamped <= 2 -> clamped
                    clamped <= 6 -> clamped - 1
                    clamped <= 10 -> clamped - 2
                    clamped <= 15 -> clamped - 3
                    else -> (clamped - 4).coerceAtMost(digits.length)
                }
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        if (digits.isNotEmpty()) sb.append("(")
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 1) sb.append(") ")
            else if (digits.length == 11 && i == 6) sb.append("-")
            else if (digits.length <= 10 && i == 5) sb.append("-")
        }
        val transformed = sb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceAtMost(digits.length)
                return when {
                    clamped == 0 -> 0
                    clamped <= 2 -> clamped + 1
                    clamped <= 6 -> clamped + 3
                    else -> (clamped + 4).coerceAtMost(transformed.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceAtMost(transformed.length)
                return when {
                    clamped <= 1 -> 0
                    clamped <= 4 -> clamped - 1
                    clamped <= 9 -> (clamped - 3).coerceAtLeast(0)
                    else -> (clamped - 4).coerceAtMost(digits.length)
                }
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(8)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 4) sb.append("-")
        }
        val transformed = sb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceAtMost(digits.length)
                return if (clamped <= 5) clamped else (clamped + 1).coerceAtMost(transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceAtMost(transformed.length)
                return if (clamped <= 5) clamped else (clamped - 1).coerceAtMost(digits.length)
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

