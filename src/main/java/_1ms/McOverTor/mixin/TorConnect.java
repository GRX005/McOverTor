package _1ms.McOverTor.mixin;

import _1ms.McOverTor.ConnectingScreen;
import io.netty.channel.Channel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(targets = "net/minecraft/network/ClientConnection$1")
public class TorConnect {
    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void connect(Channel channel, CallbackInfo ci) {
        if(ConnectingScreen.progress == 100) {
            channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress("127.0.0.1", 9050))); //TODO See if the error when the server's not available can be removed.
        }
    }
}