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
        val raw = text.text
        val rawToDigitIndex = IntArray(raw.length + 1)
        val digitsSb = StringBuilder()

        for (i in raw.indices) {
            rawToDigitIndex[i] = digitsSb.length
            if (raw[i].isDigit() && digitsSb.length < 11) {
                digitsSb.append(raw[i])
            }
        }
        rawToDigitIndex[raw.length] = digitsSb.length

        val digits = digitsSb.toString()
        val transformedSb = StringBuilder()
        val digitToTrans = IntArray(digits.length + 1)
        val transToDigit = ArrayList<Int>()

        for (i in digits.indices) {
            digitToTrans[i] = transformedSb.length
            transformedSb.append(digits[i])
            transToDigit.add(i)

            if (i == 2 || i == 5) {
                if (i < digits.length - 1) {
                    transformedSb.append('.')
                    transToDigit.add(i + 1)
                }
            } else if (i == 8) {
                if (i < digits.length - 1) {
                    transformedSb.append('-')
                    transToDigit.add(i + 1)
                }
            }
        }
        digitToTrans[digits.length] = transformedSb.length
        transToDigit.add(digits.length)

        val transformed = transformedSb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedRaw = offset.coerceIn(0, raw.length)
                val digitIdx = rawToDigitIndex[clampedRaw]
                return digitToTrans[digitIdx].coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedTrans = offset.coerceIn(0, transformed.length)
                val digitIdx = transToDigit[clampedTrans]
                var rawIdx = 0
                while (rawIdx < raw.length && rawToDigitIndex[rawIdx] < digitIdx) {
                    rawIdx++
                }
                return rawIdx.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class CnpjVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val rawToDigitIndex = IntArray(raw.length + 1)
        val digitsSb = StringBuilder()

        for (i in raw.indices) {
            rawToDigitIndex[i] = digitsSb.length
            if (raw[i].isDigit() && digitsSb.length < 14) {
                digitsSb.append(raw[i])
            }
        }
        rawToDigitIndex[raw.length] = digitsSb.length

        val digits = digitsSb.toString()
        val transformedSb = StringBuilder()
        val digitToTrans = IntArray(digits.length + 1)
        val transToDigit = ArrayList<Int>()

        for (i in digits.indices) {
            digitToTrans[i] = transformedSb.length
            transformedSb.append(digits[i])
            transToDigit.add(i)

            if (i == 1 || i == 4) {
                if (i < digits.length - 1) {
                    transformedSb.append('.')
                    transToDigit.add(i + 1)
                }
            } else if (i == 7) {
                if (i < digits.length - 1) {
                    transformedSb.append('/')
                    transToDigit.add(i + 1)
                }
            } else if (i == 11) {
                if (i < digits.length - 1) {
                    transformedSb.append('-')
                    transToDigit.add(i + 1)
                }
            }
        }
        digitToTrans[digits.length] = transformedSb.length
        transToDigit.add(digits.length)

        val transformed = transformedSb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedRaw = offset.coerceIn(0, raw.length)
                val digitIdx = rawToDigitIndex[clampedRaw]
                return digitToTrans[digitIdx].coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedTrans = offset.coerceIn(0, transformed.length)
                val digitIdx = transToDigit[clampedTrans]
                var rawIdx = 0
                while (rawIdx < raw.length && rawToDigitIndex[rawIdx] < digitIdx) {
                    rawIdx++
                }
                return rawIdx.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val rawToDigitIndex = IntArray(raw.length + 1)
        val digitsSb = StringBuilder()

        for (i in raw.indices) {
            rawToDigitIndex[i] = digitsSb.length
            if (raw[i].isDigit() && digitsSb.length < 11) {
                digitsSb.append(raw[i])
            }
        }
        rawToDigitIndex[raw.length] = digitsSb.length

        val digits = digitsSb.toString()
        val transformedSb = StringBuilder()
        val digitToTrans = IntArray(digits.length + 1)
        val transToDigit = ArrayList<Int>()

        if (digits.isNotEmpty()) {
            transformedSb.append('(')
            transToDigit.add(0)
        }

        for (i in digits.indices) {
            digitToTrans[i] = transformedSb.length
            transformedSb.append(digits[i])
            transToDigit.add(i)

            if (i == 1) {
                transformedSb.append(") ")
                transToDigit.add(i + 1)
                transToDigit.add(i + 1)
            } else if (digits.length == 11 && i == 6) {
                transformedSb.append('-')
                transToDigit.add(i + 1)
            } else if (digits.length <= 10 && i == 5) {
                transformedSb.append('-')
                transToDigit.add(i + 1)
            }
        }
        digitToTrans[digits.length] = transformedSb.length
        transToDigit.add(digits.length)

        val transformed = transformedSb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedRaw = offset.coerceIn(0, raw.length)
                val digitIdx = rawToDigitIndex[clampedRaw]
                return digitToTrans[digitIdx].coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedTrans = offset.coerceIn(0, transformed.length)
                val digitIdx = transToDigit[clampedTrans]
                var rawIdx = 0
                while (rawIdx < raw.length && rawToDigitIndex[rawIdx] < digitIdx) {
                    rawIdx++
                }
                return rawIdx.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

class CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val rawToDigitIndex = IntArray(raw.length + 1)
        val digitsSb = StringBuilder()

        for (i in raw.indices) {
            rawToDigitIndex[i] = digitsSb.length
            if (raw[i].isDigit() && digitsSb.length < 8) {
                digitsSb.append(raw[i])
            }
        }
        rawToDigitIndex[raw.length] = digitsSb.length

        val digits = digitsSb.toString()
        val transformedSb = StringBuilder()
        val digitToTrans = IntArray(digits.length + 1)
        val transToDigit = ArrayList<Int>()

        for (i in digits.indices) {
            digitToTrans[i] = transformedSb.length
            transformedSb.append(digits[i])
            transToDigit.add(i)

            if (i == 4) {
                if (i < digits.length - 1) {
                    transformedSb.append('-')
                    transToDigit.add(i + 1)
                }
            }
        }
        digitToTrans[digits.length] = transformedSb.length
        transToDigit.add(digits.length)

        val transformed = transformedSb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedRaw = offset.coerceIn(0, raw.length)
                val digitIdx = rawToDigitIndex[clampedRaw]
                return digitToTrans[digitIdx].coerceIn(0, transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedTrans = offset.coerceIn(0, transformed.length)
                val digitIdx = transToDigit[clampedTrans]
                var rawIdx = 0
                while (rawIdx < raw.length && rawToDigitIndex[rawIdx] < digitIdx) {
                    rawIdx++
                }
                return rawIdx.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

