package com.perevodchik.clickerapp.network

import android.annotation.SuppressLint
import com.perevodchik.clickerapp.loge
import com.perevodchik.clickerapp.logwtf
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.net.URI


class WebSocketClient0 {

    companion object {
        var isRunning = false
        var isAuthorized= false
        var url = ""
        val URL = System.getProperty("url", "ws://195.201.109.34:1488/ws")
        var id = ""
        var channel: Channel? = null

        fun reconnect() {
            isAuthorized = false
            if (isRunning) {
                isRunning = false
                channel?.disconnect()
            }
            channel = null
            Thread {
                val ws = WebSocketClient0()
                ws.connect0(isLogin = true)
            }.apply { start() }
        }

        fun connect(message: String = "", isLogin: Boolean = false) {
            Thread {
                val ws = WebSocketClient0()
                ws.connect0(message, isLogin)
            }.apply { start() }
        }

        fun disconnect() {
            channel?.close()
//            channel?.writeAndFlush(CloseWebSocketFrame())
//            channel?.closeFuture()?.sync()
            isAuthorized = false
            isRunning = false
            channel = null
            "end".loge("STATE>DISCONNECT")
        }
    }

    @SuppressLint("DefaultLocale")
    fun connect0(message: String = "", isLogin: Boolean = false) {
       try {
           val uri = URI(System.getProperty("url", url)!!)
           val scheme = if (uri.scheme == null) "ws" else uri.scheme
           val host = if (uri.host == null) "195.201.109.34" else uri.host
           val port: Int

           port = if (uri.port == -1) {
               when {
                   "ws".equals(scheme, ignoreCase = true) -> {
                       80
                   }
                   "wss".equals(scheme, ignoreCase = true) -> {
                       443
                   }
                   else -> {
                       -1
                   }
               }
           } else uri.port

           if (!"ws".equals(scheme, ignoreCase = true) && !"wss".equals(scheme, ignoreCase = true)) {
               System.err.println("Only WS(S) is supported.")
               return
           }

           val ssl = "wss".equals(scheme, ignoreCase = true)
           val sslCtx: SslContext?
           sslCtx = if (ssl) {
               SslContextBuilder.forClient()
                   .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
           } else null

           val group: EventLoopGroup = NioEventLoopGroup()
           try {
               val handler =
                   WebSocketClientHandler(
                       WebSocketClientHandshakerFactory.newHandshaker(
                           uri, WebSocketVersion.V13, null, true, DefaultHttpHeaders()
                       )
                   )
               val b = Bootstrap()
               b.group(group).apply {
                   channel(NioSocketChannel::class.java)
                   handler(object : ChannelInitializer<SocketChannel>() {
                       override fun initChannel(ch: SocketChannel) {
                           val p = ch.pipeline()
                           if (sslCtx != null) {
                               p.addLast(sslCtx.newHandler(ch.alloc(), host, port))
                           }
                           p.addLast(
                               HttpClientCodec(),
                               HttpObjectAggregator(8192),
                               WebSocketClientCompressionHandler.INSTANCE,
                               handler
                           )
                       }
                   })
               }
               channel = b.connect(uri.host, port).sync().channel()
               handler.handshakeFuture()?.sync()

               if(message.isNotEmpty())
                   channel?.writeAndFlush(TextWebSocketFrame(message))
               if(isLogin)
                   login0(channel)

               handler.handshakeFuture()?.channel()?.closeFuture()?.sync()
               isRunning = true
           } finally {
               group.shutdownGracefully()
           }
       } catch(exxx: IllegalArgumentException) {
           exxx.localizedMessage.loge("IllegalArgumentException")
       }
    }

    private fun login0(c: Channel?) {
        c?.writeAndFlush(TextWebSocketFrame("{\"data\":{\"type\":\"login\",\"client_id\":\"$id\"}}"))
    }
}