package com.redhat.datagrid.graph.cache;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    @Value("${infinispan.list_servers:localhost:11222}")
    private String list_servers; // host1[:port][;host2[:port]]

    private RemoteCacheManager rcm;
    
    @PostConstruct
    void init(){
        // HotRod ConfigurationBuilder.
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        // Make sure to register the ProtoStreamMarshaller.
        configurationBuilder.addServers(this.list_servers);

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
