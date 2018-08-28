package com.redhat.datagrid.graph.service;

import com.redhat.datagrid.graph.cache.RemoteCacheManagerFactory;
import com.redhat.datagrid.graph.mbean.MBeanServer;
import com.redhat.datagrid.graph.mbean.RefreshServersThread;
import com.redhat.datagrid.graph.model.CacheInfo;
import com.redhat.datagrid.graph.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by mcouliba on 23/07/2018.
 */
@Service
public class ClusterInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterInfoService.class);

    @Autowired
    private RemoteCacheManagerFactory remoteCacheManagerFactory;

    private Map<InetSocketAddress, MBeanServer> mbeanServers;
    private RefreshServersThread refreshServersThread;

    public ClusterInfoService() throws Exception {
        mbeanServers = new HashMap<InetSocketAddress, MBeanServer>();
    }

    @PostConstruct
    private void init() throws Exception {
//        refreshServersThread = new RefreshServersThread(this);
//        refreshServersThread.start();
        this.updateMBeanServers();
    }

    public void updateMBeanServers() throws Exception {
        List<InetSocketAddress> socketAddresses = remoteCacheManagerFactory.getServer();

        // Remove MBeanServers if no more in the cluster
        for (InetSocketAddress socketAddress : mbeanServers.keySet()){
            if (!socketAddresses.contains(socketAddress)) {
                mbeanServers.get(socketAddress).close();
                mbeanServers.remove(socketAddress);
            }
        }

        // Add new MBeanServers only
        for (InetSocketAddress socketAddress: socketAddresses){
            if (!mbeanServers.containsKey(socketAddresses)) {
                LOGGER.debug("Adding new MBeanServer " + socketAddress);
                MBeanServer mbeanServer = new MBeanServer(socketAddress);

                mbeanServers.put(socketAddress, mbeanServer);
                LOGGER.debug("MBeanServer " + socketAddress + " Adding");
            }
            else {
                LOGGER.debug("MBeanServer " + socketAddress + " already added");
            }
        }
    }

    public List<NodeInfo> getNodeInfo(String cacheName, String containerName) throws Exception {
        List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();

        for (MBeanServer mbeanServer: mbeanServers.values()){
            nodeInfos.add(mbeanServer.getNodeInfo(cacheName, containerName));
        }

        return nodeInfos;
    }

    public List<CacheInfo> getCacheInfo() throws Exception {
        List<CacheInfo> cacheInfos = null;

        Map.Entry<InetSocketAddress, MBeanServer> firstEntry = mbeanServers.entrySet().iterator().next();
        if (firstEntry != null) {
            MBeanServer mbeanServer = firstEntry.getValue();
            cacheInfos = mbeanServer.getCacheInfo();
        }

        return cacheInfos;
    }

    public List<NodeInfo> getDemoNodeInfo() throws Exception {
        List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        int nodeNumber = new Random().nextInt(5) + 1;

        IntStream.rangeClosed(1, nodeNumber).forEach(i -> {
            int entryNumber = new Random().nextInt(10);
            NodeInfo newNodeInfo = new NodeInfo("DemoNode_" + i, null, entryNumber);
            nodeInfos.add(newNodeInfo);
        });

        return nodeInfos;
    }

    public List<CacheInfo> getDemoCacheInfo() throws Exception {
        List<CacheInfo> cacheInfos = new ArrayList<CacheInfo>();

        CacheInfo newCacheInfo = new CacheInfo("Demo", "Demo");
        cacheInfos.add(newCacheInfo);
        return cacheInfos;
    }

    public boolean isDemoMode (){
        return remoteCacheManagerFactory.isDemoMode();
    }
}
