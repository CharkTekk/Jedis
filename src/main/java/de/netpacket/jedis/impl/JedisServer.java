package de.netpacket.jedis.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.netpacket.jedis.IServer;
import de.netpacket.jedis.JedisConnection;
import de.netpacket.jedis.strategy.RoundRobinBalancingStrategy;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.Set;

public class JedisServer implements IServer, AutoCloseable{

    private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create();

    private final RoundRobinBalancingStrategy balancingStrategy;
    private final JedisConnection connection;
    private final Integer redisPoolsAmount;
    private JedisPool[] pools;

    public JedisServer(JedisConnection connection, Integer redisPoolsAmount) {
        this.connection = connection;
        this.redisPoolsAmount = redisPoolsAmount;
        this.pools = new JedisPool[redisPoolsAmount];
        this.pools = jedisPools();
        this.balancingStrategy = new RoundRobinBalancingStrategy(pools);
    }

    private JedisPool[] jedisPools(){
        for (int i = 0; i < redisPoolsAmount; i++) {
            pools[i] = connection.getPassword().equals(" ") ?
                    new JedisPool(connection.getAddress(), connection.getPort()) :
                    new JedisPool(new GenericObjectPoolConfig<>(), connection.getAddress(), connection.getPort(), 120, connection.getPassword());
        }
        return pools;
    }

    @Override
    public String split() {
        return "~";
    }

    @Override
    public void set(String key, Object object) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            jedis.set(key, GSON.toJson(object));
        }
    }

    @Override
    public void set(String key, Object value, long seconds) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            jedis.set(key, GSON.toJson(value), SetParams.setParams().ex(seconds));
        }
    }


    @Override
    public void del(String key) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            jedis.del(key);
        }
    }

    @Override
    public void sendToHandler(String channel, String... message) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            jedis.publish(channel, Arrays.toString(message));
        }
    }

    @Override
    public void sendToHandler(String channel, String message, Object object) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            jedis.publish(channel, message + "~" + GSON.toJson(object));
        }
    }

    @Override
    public void setupSubscriber(JedisPubSub jedisPubSub, String... channels) {
        new Thread(() -> {
            while(true) {
                try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
                    jedis.subscribe(jedisPubSub, channels);
                }
            }
        }).start();
    }



    @Override
    public Object getServerObject(String key, Class<?> cls) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            return GSON.fromJson(jedis.get(key), cls);
        }
    }

    @Override
    public Object getObjectFromString(String key, Class<?> cls) {
        return GSON.fromJson(key, cls);
    }

    @Override
    public Set<String> keys(String pattern) {
        try(Jedis jedis = balancingStrategy.selectPool().getResource()) {
            return jedis.keys(pattern);
        }
    }

    @Override
    public void close() throws Exception {
        for (JedisPool pool : pools) {
            if(pool.getResource().isConnected())
                pool.close();
        }
    }
}
