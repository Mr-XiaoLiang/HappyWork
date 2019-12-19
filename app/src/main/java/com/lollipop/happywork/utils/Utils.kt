package com.lollipop.happywork.utils

import java.lang.StringBuilder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author lollipop
 * @date 2019-12-19 22:47
 * 工具类
 */

fun String.md5(): String {
    try {
        //获取md5加密对象
        val instance = MessageDigest.getInstance("MD5")
        //对字符串加密，返回字节数组
        val digest:ByteArray = instance.digest(this.toByteArray())
        val sb = StringBuilder()
        for (b in digest) {
            //获取低八位有效值
            val i :Int = b.toInt() and 0xff
            //将整数转化为16进制
            val hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                sb.append("0")
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}