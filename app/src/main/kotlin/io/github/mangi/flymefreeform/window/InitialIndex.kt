package io.github.mangi.flymefreeform.window

import java.nio.charset.Charset

/**
 * @author bomo 应用 label 首字母分组索引。
 *
 * ASCII 字母取大写；中文按 GB2312 区位查 [PinyinInitialTable] 全字库拼音首字母表
 * （覆盖 GB2312 全部 6763 个汉字，含二级/生僻字，如「哔」→ B）；
 * 其余符号、数字、无法识别字符归入 '#'。
 */
object InitialIndex {

    private val GB2312 = Charset.forName("GB2312")

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
        val index = (hi - 0xB0) * 94 + (lo - 0xA1)
        val initial = PinyinInitialTable.TABLE[index]
        return initial.takeIf { it != '#' }
    }
}
