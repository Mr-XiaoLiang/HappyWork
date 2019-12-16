package com.lollipop.happywork.core

import java.net.InetSocketAddress
import java.net.Socket


/**
 * @author lollipop
 * @date 2019-12-10 21:25
 * 连接器的客户端
 */
class ConnectionClient(private val callback: ClientCallback) {

    companion object {
        const val CONNECT_TIME_OUT = 60 * 1000
    }

    private var connectTask: ConnectTask? = null

    fun connect(ip: String, port: Int): Runnable {
        if (!isStop) {
            connectTask?.stop()
        }
        val task = ConnectTask(InetSocketAddress(ip, port), {
            // onConnected
            callback.onConnected(this)
        }, {
            // onMessage
            callback.onMessage(this, it)
        }, {
            // onDisconnected
            callback.onDisconnect(this)
        }, {
            // onError
            callback.onError(this, it)
        })
        connectTask = task
        return task
    }

    val isRunning: Boolean
        get() {
            return connectTask?.isRunning == true
        }

    val isStop: Boolean
        get() {
            return connectTask?.isStop == true
        }

    /**
     * 连接器对象
     */
    private class ConnectTask(private val address: InetSocketAddress,
                              private val connectedListener: (() -> Unit),
                              private val messageListener: ((Info) -> Unit),
                              private val disconnectedListener: (() -> Unit),
                              private val errorListener: ((Throwable) -> Unit)? = null): Runnable {

        private val socket: Socket = Socket()

        var isStop = false
            private set

        val isRunning: Boolean
            get() {
                return !isStop && !socket.isClosed
                        && !socket.isOutputShutdown
                        && !socket.isInputShutdown
            }

        fun write(info: Info) {
            if (isStop || socket.isClosed || socket.isOutputShutdown) {
                return
            }
            InfoAgreement.write(socket.getOutputStream(), info)
        }

        fun stop() {
            if (!isStop && !socket.isClosed) {
                socket.close()
            }
            this.isStop = true
        }

        override fun run() {
            try {
                socket.connect(address, CONNECT_TIME_OUT)
                if (isStop) {
                    return
                }
                connectedListener()
                val inputStream = socket.getInputStream()
                while (!isStop && !socket.isClosed && !socket.isInputShutdown) {
                    val info = InfoAgreement.read(inputStream)
                    if (isStop) {
                        return
                    }
                    if (info.isEffective) {
                        messageListener(info)
                    } else if (info.type == Info.TYPE_END) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorListener?.invoke(e)
            } finally {
                if (!isStop) {
                    isStop = true
                }
                disconnectedListener()
                try {
                    socket.close()
                } catch (e: Exception) {}
            }
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

        /**
         * 当发生异常时
         */
        fun onError(client: ConnectionClient, error: Throwable)
    }

}