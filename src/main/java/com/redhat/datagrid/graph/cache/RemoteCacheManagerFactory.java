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

    @Value("${infinispan.list_servers}")
    private String list_servers; // host1[:port][;host2[:port]]

    private boolean demoMode = false;

    private RemoteCacheManager rcm;

    @PostConstruct
    void init(){
        if ((this.list_servers != null) && !("".equals(this.list_servers))) {
            // HotRod ConfigurationBuilder.
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

            configurationBuilder.addServers(this.list_servers);
            rcm = new RemoteCacheManager(configurationBuilder.build());
        } else {
            LOGGER.info("--- DEMO MODE ENABLED ---");
            demoMode = true;
        }
    }

    public List<InetSocketAddress> getServer() {
        List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

        if (rcm != null) {
            rcm.getCache().stats();

            Set<SocketAddress> socketAddresses = rcm.getCache().getCacheTopologyInfo().getSegmentsPerServer().keySet();

            for (SocketAddress socketAddress : socketAddresses) {
                servers.add((InetSocketAddress) socketAddress);
            }
        }

        return servers;
    }

    public boolean isDemoMode (){
        return this.demoMode;
    }
}
