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

import _1ms.McOverTor.Main;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListeners;
import com.google.common.cache.RemovalNotification;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//We store the srv addresses needed for bypassing mc's dns resolution and using Tor's like this.
//We generate a unique port for each ServerAddress and store it in a thread-safe Cache, which is basically a hashmap, so we make another one and keep them in sync, to optimally look up any value without iterating over all entries for each srv.
public class DnsMgr {
    private static final ConcurrentHashMap<ServerAddress, Integer> REVERSE = new ConcurrentHashMap<>();

    private static final Cache<Integer, ServerAddress> PENDING = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            // Run off-thread using Guava's native async wrapper so eviction can never
            // re-enter REVERSE while REVERSE.compute() is holding that key's lock.
            .removalListener(RemovalListeners.asynchronous(
                    (RemovalNotification<Integer, ServerAddress> n) ->
                            REVERSE.remove(Objects.requireNonNull(n.getValue()), n.getKey()),
                    Main.vExec
            ))
            .build();

    private static final AtomicInteger PORT_COUNTER = new AtomicInteger(10000);

    public static int register(ServerAddress real) {
        // Fast path: no locking, handles the overwhelming majority of calls
        // (already-registered, still-valid address).
        Integer existing = REVERSE.get(real);
        if (existing != null && PENDING.getIfPresent(existing) != null) {
            return existing;
        }
        // Slow path: only taken on first registration or after expiry.
        return REVERSE.compute(real, (key, port) -> {
            if (port != null && PENDING.getIfPresent(port) != null) {
                return port; // lost the race to another thread, reuse it
            }
            int newPort = PORT_COUNTER.updateAndGet(p -> p >= 65535 ? 10001 : p + 1);

            PENDING.put(newPort, key);
            return newPort;
        });
    }

    public static ServerAddress get(int port) {
        return PENDING.getIfPresent(port);
    }
}