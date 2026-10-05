package io.github.mangi.flymefreeform.window

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialIndexTest {

    @Test
    fun englishLabelsKeepUppercaseInitial() {
        assertEquals('A', InitialIndex.initialOf("Alipay"))
        assertEquals('W', InitialIndex.initialOf("WeChat"))
        assertEquals('Q', InitialIndex.initialOf("QQ"))
        assertEquals('Z', InitialIndex.initialOf("Zoom"))
    }

    @Test
    fun chineseLabelsMapToPinyinInitial() {
        assertEquals('Z', InitialIndex.initialOf("支付宝"))
        assertEquals('W', InitialIndex.initialOf("微信"))
        assertEquals('D', InitialIndex.initialOf("钉钉"))
        assertEquals('B', InitialIndex.initialOf("百度"))
        assertEquals('T', InitialIndex.initialOf("淘宝"))
        assertEquals('X', InitialIndex.initialOf("小红书"))
        assertEquals('J', InitialIndex.initialOf("京东"))
        assertEquals('Y', InitialIndex.initialOf("云闪付"))
        assertEquals('Y', InitialIndex.initialOf("云音乐"))
        assertEquals('Z', InitialIndex.initialOf("作业帮"))
    }

    @Test
    fun numbersAndSymbolsFallbackToHash() {
        assertEquals('#', InitialIndex.initialOf("12306"))
        assertEquals('#', InitialIndex.initialOf("&%#"))
        assertEquals('#', InitialIndex.initialOf(""))
        // 全字库表覆盖 GB2312 二级汉字（如「哔」→ B）。
        assertEquals('B', InitialIndex.initialOf("哔哩哔哩"))
        assertEquals('B', InitialIndex.pinyinInitialOf('哔'))
    }

    @Test
    fun chinesePinyinInitialCoversCommonCharacters() {
        assertEquals('Z', InitialIndex.pinyinInitialOf('支'))
        assertEquals('W', InitialIndex.pinyinInitialOf('微'))
        assertEquals('A', InitialIndex.pinyinInitialOf('阿'))
        assertEquals('Y', InitialIndex.pinyinInitialOf('云'))
        assertEquals('Z', InitialIndex.pinyinInitialOf('在'))
        assertEquals('D', InitialIndex.pinyinInitialOf('钉'))
        assertEquals(null, InitialIndex.pinyinInitialOf('A'))
    }
}
