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
import io.netty.channel.Channel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(targets = "net.minecraft.network.ClientConnection$1")
abstract class TorConnect {
    @Inject(method = "initChannel", at = @At("HEAD"))
    private void connect(Channel channel, CallbackInfo ci) {
        if(TorManager.progress == 100 || SettingsMgr.get(TorOption.torOnly)) {
            channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress("127.0.0.1", Integer.parseInt(TorManager.sPort))));
        }
    }
}