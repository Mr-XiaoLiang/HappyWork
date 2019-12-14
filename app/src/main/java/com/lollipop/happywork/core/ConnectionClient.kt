package com.lollipop.happywork.core

import java.net.InetSocketAddress
import java.net.Socket


/**
 * @author lollipop
 * @date 2019-12-10 21:25
 * 连接器的客户端
 */
open class ConnectionClient {

    companion object {
        const val CONNECT_TIME_OUT = 60 * 1000
    }

    /**
     * 连接器对象
     */
    private class ConnectTask(private val address: InetSocketAddress,
                              private val connectedListener: (() -> Unit),
                              private val messageListener: ((Info) -> Unit),
                              private val disconnectedListener: (() -> Unit)): Runnable {

        private val socket = Socket()

        private var isStop = false

        fun write(info: Info) {
            if (isStop) {
                return
            }
        }

        override fun run() {
        }

    }

    interface ClientCallback {
        /**
         * 连接已经接通
         * @param client 客户端对象
         */
        fun onConnected(client: ConnectionClient)

        /**
         * 有消息传入时回调
         * @param info 消息内容
         * @param client 连接的客户端
         */
        fun onMessage(client: ConnectionClient, info: Info)

        /**
         * 当连接断开时
         * @param client 客户端对象
         */
        fun onDisconnect(client: ConnectionClient)
    }

}