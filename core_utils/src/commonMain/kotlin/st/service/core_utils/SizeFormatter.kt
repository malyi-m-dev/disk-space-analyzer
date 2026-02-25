package st.service.core_utils

import kotlin.math.ln
import kotlin.math.pow

object SizeFormatter {
    fun format(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = listOf("KB", "MB", "GB", "TB", "PB")
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.size)
        val value = bytes / 1024.0.pow(exp.toDouble())
        return "%.1f %s".format(value, units[exp - 1])
    }
}
