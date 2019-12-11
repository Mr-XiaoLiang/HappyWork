package com.lollipop.happywork.core

/**
 * @author lollipop
 * @date 2019-12-10 21:59
 * 信息类
 */
class Info(val type: Int) {

    companion object {
        val EMPTY_ARRAY = ByteArray(0)
    }

    var bytes = EMPTY_ARRAY

    var value = ""

}