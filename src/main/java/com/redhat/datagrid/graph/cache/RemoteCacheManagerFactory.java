package com.redhat.datagrid.graph.cache;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mcouliba on 24/07/2018.
 */
@Component
public class RemoteCacheManagerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCacheManagerFactory.class);

    @Value("${infinispan.server.host:localhost}")
    private String server_host = "localhost";

    @Value("${infinispan.server.hotrod.port:11222}")
    private int server_hotrod_port = 11222;

    private RemoteCacheManager rcm;

    public RemoteCacheManagerFactory() {

        // HotRod ConfigurationBuilder.
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        // Make sure to register the ProtoStreamMarshaller.
        configurationBuilder.addServer()
                .host(server_host).port(server_hotrod_port);

        rcm = new RemoteCacheManager(configurationBuilder.build());
    }

    public List<InetSocketAddress> getServer() {
        List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
        rcm.getCache().stats();

        Set<SocketAddress> socketAddresses = rcm.getCache().getCacheTopologyInfo().getSegmentsPerServer().keySet();

        for (SocketAddress socketAddress: socketAddresses){
            servers.add((InetSocketAddress) socketAddress);
        }

        return servers;
    }
}
