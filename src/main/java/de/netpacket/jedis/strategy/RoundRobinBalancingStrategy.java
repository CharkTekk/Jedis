package de.netpacket.jedis.strategy;

import redis.clients.jedis.JedisPool;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancingStrategy extends BalancingStrategy {
    private final AtomicInteger select = new AtomicInteger(0);

    private final JedisPool[] pools;

    public RoundRobinBalancingStrategy(JedisPool... pools) {
        this.pools = pools;
    }

    @Override
    public JedisPool selectPool() {
        int now = select.getAndIncrement();
        if (now >= pools.length) {
            now = 0;
            select.set(0);
        }
        System.out.println(select);
        return pools[now];
    }
}
