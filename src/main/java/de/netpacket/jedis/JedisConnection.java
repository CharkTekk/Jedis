package de.netpacket.jedis;

public class JedisConnection {

    private final String address;
    private final String password;
    private final int port;

    public JedisConnection(String address, String password, int port) {
        this.address = address;
        this.password = password;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

}
