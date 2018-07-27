package com.redhat.datagrid.graph.mbean;

import com.redhat.datagrid.graph.model.CacheInfo;
import com.redhat.datagrid.graph.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mcouliba on 23/07/2018.
 */
public class MBeanServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanServer.class);
    private static final int DEFAULT_MANAGEMENT_PORT_OFFSET = 1232; // 11222 - 9990

    InetSocketAddress socketAddress;
    JMXConnector jmxConnector;
    MBeanServerConnection connection;


    public MBeanServer(InetSocketAddress socketAddress) throws Exception {
        this.socketAddress = socketAddress;

        String urlString ="service:jmx:remote+http://" + socketAddress.getHostName() + ":" + (socketAddress.getPort() - DEFAULT_MANAGEMENT_PORT_OFFSET);

        JMXServiceURL serviceURL = new JMXServiceURL(urlString);

        jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
        connection = jmxConnector.getMBeanServerConnection();
    }

    public void connect() throws IOException {
        jmxConnector.connect();
    }

    public void close() throws IOException {
        jmxConnector.close();
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws IOException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        return connection.getAttribute(objectName, attributeName);
    }

    public String getNodeName() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        ObjectName objectName = ObjectName.getInstance("jgroups:type=channel,cluster=\"cluster\"");
        String attributeName = "name";
        return connection.getAttribute(objectName, attributeName).toString();
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public List<CacheInfo> getCacheInfo() throws Exception{
        List<CacheInfo> cacheInfos = new ArrayList<CacheInfo>();

        Set<ObjectName> objectNames = connection
                .queryNames(new ObjectName("jboss.datagrid-infinispan:type=Cache,name=*,manager=\"clustered\",component=Cache"), null);

        String attributeName = "cacheName";

        for (ObjectName objectName : objectNames) {
            String cacheName = (String) connection.getAttribute(objectName, attributeName);
            if ((cacheName != null) && !cacheName.startsWith("___")){
                CacheInfo newCacheInfo = new CacheInfo(cacheName, null);
                cacheInfos.add(newCacheInfo);
            }
        }

        return cacheInfos;
    }

    public NodeInfo getNodeInfo(String cacheName, String containerName) throws Exception {
        // Retrieving number of entries
        ObjectName objectName =
                ObjectName.getInstance("jboss.datagrid-infinispan:type=Cache,name=\""
                        + cacheName + "\",manager=\"" + containerName + "\",component=Statistics");
        String attributeName = "numberOfEntriesInMemory";
        String numberEntries = connection.getAttribute(objectName, attributeName).toString();

        NodeInfo nodeInfo = new NodeInfo(getNodeName(), socketAddress, Long.valueOf(numberEntries));

        return nodeInfo;
    }
}
