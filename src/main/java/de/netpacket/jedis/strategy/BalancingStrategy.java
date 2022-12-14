package de.netpacket.jedis.strategy;

import redis.clients.jedis.JedisPool;

public abstract class BalancingStrategy {

    public abstract JedisPool selectPool();
}
