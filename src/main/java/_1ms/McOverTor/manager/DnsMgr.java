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

package _1ms.McOverTor.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import net.minecraft.client.network.ServerAddress;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//We store the srv addresses needed for bypassing mc's dns resolution and using Tor's like this.
//We generate a unique port for each ServerAddress and store it in a thread-safe Cache, which is basically a hashmap, so we make another one and keep them in sync, to optimally look up any value without iterating over all entries for each srv.
public class DnsMgr {

    private static final ConcurrentHashMap<ServerAddress, Integer> REVERSE = new ConcurrentHashMap<>();
    private static final Cache<Integer, ServerAddress> PENDING = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener((RemovalNotification<Integer, ServerAddress> n) ->
                    REVERSE.remove(Objects.requireNonNull(n.getValue())))
            .build();

    private static final AtomicInteger PORT_COUNTER = new AtomicInteger(10000);

    public static int register(ServerAddress real) {
        Integer existing = REVERSE.get(real);
//        System.out.println("P: "+PENDING.asMap());
//        System.out.println("R: "+REVERSE);
        if (existing != null && PENDING.getIfPresent(existing) != null)
            return existing;

        int port = PORT_COUNTER.updateAndGet(p -> p >= 65535 ? 10001 : p + 1);
        PENDING.put(port, real);
        REVERSE.put(real, port);

        return port;
    }

    public static ServerAddress get(int port) {
        return PENDING.getIfPresent(port);
    }

}
