package com.lollipop.happywork.core

import java.net.ServerSocket

/**
 * @author lollipop
 * @date 2019-12-12 22:20
 * 连接器的服务端
 */
class ConnectionService(
    port: Int,
    private val handler: InfoHandler): Runnable {

    private val serverSocket = ServerSocket(port)

    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}