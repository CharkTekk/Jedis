package de.netpacket.jedis;

import redis.clients.jedis.JedisPubSub;

import java.util.Set;

public interface IServer {

    String split();

    void set(String key, Object object);

    void set(String key, Object object, long deleteDate);

    void del(String key);

    void sendToHandler(String channel, String... message);

    void sendToHandler(String channel, String message, Object object);

    void setupSubscriber(JedisPubSub jedisPubSub, String... channels);

    Object getServerObject(String key, Class<?> cls);

    Object getObjectFromString(String key, Class<?> cls);

    Set<String> keys(String pattern);

}
