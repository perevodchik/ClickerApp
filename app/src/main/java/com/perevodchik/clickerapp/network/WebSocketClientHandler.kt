package com.perevodchik.clickerapp.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.perevodchik.clickerapp.loge
import com.perevodchik.clickerapp.logwtf
import com.perevodchik.clickerapp.model.pattern.Base
import com.perevodchik.clickerapp.service.ClickerService
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.websocketx.*
import io.netty.util.CharsetUtil


class WebSocketClientHandler(handshaker0: WebSocketClientHandshaker): SimpleChannelInboundHandler<Any>() {
    private var handshaker: WebSocketClientHandshaker? = handshaker0
    private var handshakeFuture: ChannelPromise? = null

    fun handshakeFuture(): ChannelFuture? {
        return handshakeFuture
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        handshakeFuture = ctx.newPromise()
        super.handlerAdded(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        "channelActive".loge()
        handshaker!!.handshake(ctx.channel())
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        "channelInactive".loge()
//        if(!WebSocketClient0.isForceStopped) WebSocketClient0.connect(isLogin = true)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        super.channelUnregistered(ctx)
        "channelUnregistered".loge()
//        if(!WebSocketClient0.isForceStopped) WebSocketClient0.connect(isLogin = true)
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        val ch = ctx.channel()
        if (!handshaker!!.isHandshakeComplete) {
            try {
                handshaker?.finishHandshake(ch, msg as FullHttpResponse?)
               "WebSocket Client connected!".logwtf()
                handshakeFuture!!.setSuccess()
            } catch (e: WebSocketHandshakeException) {
              "WebSocket Client failed to connect".logwtf()
                handshakeFuture!!.setFailure(e)
            }
            return
        }

        when (val frame = msg as WebSocketFrame?) {
            is TextWebSocketFrame -> {
                val text = frame.text()
                text.loge("received text")

                when {
                    text.equals("Client successfully connected to server", true) -> {
                        WebSocketClient0.isAuthorized = true
                        "Channel running".loge("STATE")
                    }
                    text.equals("Templates saved successfully", true) -> {
                        "Export successfully".loge("STATE>EXPORT>SUCCESS")
                    }
                    text.startsWith("Wrong template name : The duplicate key value is", true) -> {
                        "Duplicate key".loge("STATE>EXPORT>ERROR")
                    }
                    else -> {
                        var base: Base? = null
                        try {
                            base = Gson().fromJson(text, Base::class.java)
                        } catch (jsonEx: JsonSyntaxException) { jsonEx.localizedMessage?.loge("JsonSyntaxException") }
                        if(base != null)
                            ClickerService.clickerService?.addBase(base)
                    }
                }
            }
            is PingWebSocketFrame -> {
                "pong".logwtf("ping")
            }
            is PongWebSocketFrame -> {
                "WebSocket Client received pong".logwtf()
            }
            is CloseWebSocketFrame -> {
               "WebSocket Client received closing".logwtf()
                ch.close()
            }
            else -> {
                frame?.toString()?.logwtf("else frame")
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        "exceptionCaught".loge()
        cause.printStackTrace()
        if (!handshakeFuture!!.isDone) {
            handshakeFuture!!.setFailure(cause)
        }
        ctx.close()
    }

}