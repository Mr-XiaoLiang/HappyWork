package com.lollipop.happywork.core

import com.lollipop.happywork.utils.md5
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
        callback.onDisconnected(tag)
    }

    private fun onClientError(tag: String, error: Throwable) {
        callback.onError(tag, error)
    }

    private fun removeTask(tag: String) {
        clientTaskMap.remove(tag)?.stop()
    }

    private fun createTag(socket: Socket): String {
        return "Client-${socket.inetAddress.hostAddress}:${socket.port}".md5()
    }

    private fun addTask(socket: Socket) {
        val tag = createTag(socket)
        val task = ClientTask(socket, handler, tag,
            {
                removeTask(it)
                onClientRemove(it)
            },
            { t, error ->
                removeTask(t)
                onClientError(t, error)
            })
        clientTaskMap[tag] = task
        callback.onConnected(tag)
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

    fun write(tag: String, info: Info) {
        clientTaskMap[tag]?.let {
            if (it.isRunning) {
                it.write(info)
            } else {
                clientTaskMap.remove(tag)
            }
        }
    }

    interface ServiceCallback {
        fun onConnected(tag: String)
        fun onDisconnected(tag: String)
        fun onError(tag: String, error: Throwable)
    }

    private class ClientTask(private val socket: Socket,
                             private val handler: InfoHandler,
                             private val tag: String,
                             private val disconnectedListener: ((String) -> Unit),
                             private val errorListener: ((String, Throwable) -> Unit)? = null): Runnable {

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
                errorListener?.invoke(tag, e)
            } finally {
                if (!isStop) {
                    isStop = true
                }
                disconnectedListener(tag)
                try {
                    socket.close()
                } catch (e: Exception) {}
            }
        }

    }

}