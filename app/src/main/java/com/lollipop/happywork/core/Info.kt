package com.lollipop.happywork.core

/**
 * @author lollipop
 * @date 2019-12-10 21:59
 * 信息类
 */
class Info(val type: Int) {

    companion object {
        val EMPTY_ARRAY = ByteArray(0)
        const val TYPE_END = -1
        const val TYPE_NOTING = 0
        const val TYPE_STRING = 1
        const val TYPE_PROTOBUF = 2
    }

    var bytes = EMPTY_ARRAY

    var value = ""

    val isEffective: Boolean
        get() {
            return type != TYPE_NOTING && type != TYPE_END
        }

}