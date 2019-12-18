package com.lollipop.happywork.core

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executor

/**
 * @author lollipop
 * @date 2019-12-12 22:20
 * 连接器的服务端
 */
class ConnectionService(
    port: Int,
    private val handler: InfoHandler,
    private val threadPool: Executor,
    private val callback: ServiceCallback): Runnable {

    private val serverSocket = ServerSocket(port)

    private val clientTaskMap = HashMap<String, ClientTask>()

    private var maxCount = -1

    private var isWaiting = false

    fun changeMaxCount(count: Int) {
        maxCount = count
        checkClient()
    }

    fun checkClient() {
        val entries = clientTaskMap.entries.iterator()
        while (entries.hasNext()) {
            val entry = entries.next()
            if (!entry.value.isRunning) {
                onClientRemove(entry.key)
                entries.remove()
            }
        }
        if (needWait()) {
            startWait()
        }
    }

    private fun startWait() {
        if (isWaiting) {
            return
        }
        isWaiting = true
        threadPool.execute(this)
    }

    private fun onClientRemove(tag: String) {
        // TODO
    }

    private fun addTask(socket: Socket) {
        // TODO

    }

    private fun needWait(): Boolean {
        return maxCount < 0 || clientTaskMap.size < maxCount
    }

    override fun run() {
        while (needWait()) {
            val socket = serverSocket.accept()
            addTask(socket)
        }
        isWaiting = false
    }

    interface ServiceCallback {
        fun onConnected(tag: String)
        fun onDisconnected(tag: String)
        fun onError(tag: String, error: Throwable)
    }

    private class ClientTask(private val socket: Socket,
                             private val handler: InfoHandler,
                             private val tag: String,
                             private val disconnectedListener: (() -> Unit),
                             private val errorListener: ((Throwable) -> Unit)? = null): Runnable {

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
                if (isStop) {
                    return
                }
                val inputStream = socket.getInputStream()
                while (!isStop && !socket.isClosed && !socket.isInputShutdown) {
                    val info = InfoAgreement.read(inputStream)
                    if (isStop) {
                        return
                    }
                    if (info.isEffective) {
                        handler.sendMessage(tag, info)
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

}