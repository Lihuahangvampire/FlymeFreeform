package io.github.mangi.flymefreeform.window

import java.nio.charset.Charset

/**
 * @author bomo 应用 label 首字母分组索引。
 *
 * ASCII 字母取大写；中文等 CJK 字符按 GB2312 区位码映射到拼音首字母
 * （GB2312 汉字按拼音序编码，区位区间与首字母一一对应，零依赖纯算法）；
 * 其余符号、数字、无法识别字符归入 '#'。
 */
object InitialIndex {

    private val GB2312 = Charset.forName("GB2312")

    private val initialRanges: List<Pair<Char, IntRange>> =
        listOf(
            'A' to 0xB0A1..0xB0C4,
            'B' to 0xB0C5..0xB2C0,
            'C' to 0xB2C1..0xB4ED,
            'D' to 0xB4EE..0xB6E9,
            'E' to 0xB6EA..0xB7A1,
            'F' to 0xB7A2..0xB8C0,
            'G' to 0xB8C1..0xB9FD,
            'H' to 0xB9FE..0xBBF6,
            'J' to 0xBBF7..0xBFA5,
            'K' to 0xBFA6..0xC0AB,
            'L' to 0xC0AC..0xC2E7,
            'M' to 0xC2E8..0xC4C2,
            'N' to 0xC4C3..0xC5B5,
            'O' to 0xC5B6..0xC5BD,
            'P' to 0xC5BE..0xC6D9,
            'Q' to 0xC6DA..0xC8BA,
            'R' to 0xC8BB..0xC8F5,
            'S' to 0xC8F6..0xCBF9,
            'T' to 0xCBFA..0xCDD9,
            'W' to 0xCDDA..0xCEF3,
            'X' to 0xCEF4..0xD188,
            'Y' to 0xD189..0xD4D0,
            'Z' to 0xD4D1..0xD7F9,
        )

    /** label 首字符对应的分组字母（A-Z / '#'）。 */
    fun initialOf(label: String): Char {
        val code = label.firstOrNull()?.code ?: return '#'
        if (code in 'A'.code..'Z'.code) return code.toChar()
        if (code in 'a'.code..'z'.code) return (code - 32).toChar()
        return if (code > 0xFF) {
            pinyinInitialOf(label.first()) ?: '#'
        } else {
            '#'
        }
    }

    /** 单个汉字（CJK）的拼音首字母；非 GB2312 汉字或无法识别时返回 null。 */
    fun pinyinInitialOf(character: Char): Char? {
        if (character.code <= 0xFF) return null
        val bytes =
            try {
                character.toString().toByteArray(GB2312)
            } catch (_: Exception) {
                return null
            }
        if (bytes.size != 2) return null
        val hi = bytes[0].toInt() and 0xFF
        val lo = bytes[1].toInt() and 0xFF
        if (hi !in 0xB0..0xF7) return null
        val code = (hi shl 8) or lo
        return initialRanges.firstOrNull { code in it.second }?.first
    }
}
