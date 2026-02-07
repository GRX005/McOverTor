/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2026 _1ms (GRX005)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package _1ms.McOverTor.mixin;

import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static _1ms.McOverTor.manager.DnsMgr.get;
import static _1ms.McOverTor.manager.DnsMgr.register;
import static _1ms.McOverTor.manager.TorManager.progress;

//Allow the client to connect to .onion server addrs by making it not get DNS resolved, and directly passed to the running Tor client. (Credits for the original idea to https://github.com/Debuggingss/OnionConnect)

//In the connectscreen gui at where it initializes contact to the given server, the arg (orig addr) of the func which tries to resolve the addr is replaced with 127.0.0.1:9050, and saves it in a threadLocal

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
abstract class ChInit {
    @ModifyArg(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/AllowedAddressResolver;resolve(Lnet/minecraft/client/network/ServerAddress;)Ljava/util/Optional;"
            )
    )
    private ServerAddress modifyServerAddress(ServerAddress original) {
        if(progress == 100 && SettingsMgr.get(TorOption.useTorDNS)) {
            int uniquePort = register(original);
            return new ServerAddress("127.0.0.1", uniquePort);
        }
        return original;
    }

}

//Does the same as the above but in the server pinger function.
@Mixin(MultiplayerServerListPinger.class)
abstract class FixPing {
    @ModifyArg(
            method = "add",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/AllowedAddressResolver;resolve(Lnet/minecraft/client/network/ServerAddress;)Ljava/util/Optional;"
            )
    )
    private ServerAddress modifyServerAddress(ServerAddress original) {
        if(progress==100 && SettingsMgr.get(TorOption.useTorDNS)){
            int uniquePort = register(original);
            return new ServerAddress("127.0.0.1", uniquePort);
        }
        return original;
    }
}
//Connect to the address, and disable DNS resolving
@Mixin(value = Bootstrap.class, remap = false)
abstract class NettyNoDNS {
    @Shadow public abstract ChannelFuture connect(SocketAddress remoteAddress);

    //In the netty connector, make it not resolve the addr once again, we can now connect to the onion addr, as netty's SocketAddress can hold more than just IPv4 and IPv6 addrs, unlike java's InetSocketAddress which mc uses.
    //Also now all DNS will be resolved by Tor.
    @Inject(method = "doResolveAndConnect0", at = @At("HEAD"), cancellable = true)
    private void forceDisableResolver(Channel channel, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise, CallbackInfoReturnable<ChannelFuture> cir) {
        if(progress == 100 && SettingsMgr.get(TorOption.useTorDNS)) {
            ChannelFuture future = channel.connect(remoteAddress, localAddress, promise);
            cir.setReturnValue(future);
        }
    }
    //Inject into connect to make it connect to our saved IP instead of the one it gets as arg from mc, doing it here makes it more compatible with other mods.
    @Inject(method = "connect(Ljava/net/InetAddress;I)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"), cancellable = true)
    public void connect(InetAddress inetHost, int inetPort, CallbackInfoReturnable<ChannelFuture> cir) {
        if(progress == 100 && SettingsMgr.get(TorOption.useTorDNS)) {
            ServerAddress ip = get(inetPort);
            if(ip != null)
                cir.setReturnValue(this.connect(InetSocketAddress.createUnresolved(ip.getAddress(), ip.getPort())));
        }
    }
}

//New: also update the IP in the Handshake packet that gets sent to the server the player is joining.
//If we don't do this in the packet the fake 127.0.0.1 IP we gave mc above would be sent, which would cause many srvs to reject the connection with "Invalid hostname", etc.
//Note that this gets called not just at connect but when pinging an srv too(for every srv in the srv list it's called), but there the packet will be sent with the correct IP by default, only when actually connecting to the srv does this fix execute.

@Mixin(HandshakeC2SPacket.class)
abstract class HandshakeFix {

    // 1. Fix Hostname
    @ModifyVariable(
            method = "<init>(ILjava/lang/String;ILnet/minecraft/network/packet/c2s/handshake/ConnectionIntent;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static String restoreHostname(String address, @Local(argsOnly = true, ordinal = 1) int port) {
        // 'port' here is the fake port (e.g., 40005) passed to the constructor
        if (TorManager.progress == 100 && SettingsMgr.get(TorOption.useTorDNS)) {
            ServerAddress ip = get(port);
            if (ip != null) return ip.getAddress();
        }
        return address;
    }

    // 2. Fix Port
    @ModifyVariable(
            method = "<init>(ILjava/lang/String;ILnet/minecraft/network/packet/c2s/handshake/ConnectionIntent;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private static int restorePort(int port) {
        if (TorManager.progress == 100 && SettingsMgr.get(TorOption.useTorDNS)) {
            ServerAddress ip = get(port);
            if (ip != null) return ip.getPort();
        }
        return port;
    }
}
