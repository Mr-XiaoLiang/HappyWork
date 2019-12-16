package com.lollipop.happywork.core

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * @author lollipop
 * @date 2019-12-16 22:10
 * Info信息的传出回调
 */
class InfoHandler(looper: Looper = Looper.getMainLooper()): Handler(looper) {

    companion object {
        private const val WHAT_INFO = 996
    }

    private var messageListener:((InfoMessage) -> Unit)? = null

    fun onHandleMessage(listener: (InfoMessage) -> Unit) {
        this.messageListener = listener
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == WHAT_INFO) {
            val info = msg.obj
            if (info is InfoMessage) {
                messageListener?.invoke(info)
            }
        }
    }

    data class InfoMessage(val tag: String, val info: Info)

}