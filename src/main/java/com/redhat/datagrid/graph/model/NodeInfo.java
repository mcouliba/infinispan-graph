package com.redhat.datagrid.graph.model;


import java.net.SocketAddress;

/**
 * Created by mcouliba on 23/07/2018.
 */
public class NodeInfo {

    String name;

    SocketAddress nodeAddress;

    long numberEntries;


    protected NodeInfo(){}

    public NodeInfo(String name, SocketAddress nodeAddress, long numberEntries) {
        this.name = name;
        this.nodeAddress = nodeAddress;
        this.numberEntries = numberEntries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocketAddress getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(SocketAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public long getNumberEntries() {
        return numberEntries;
    }

    public void setNumberEntries(long numberEntries) {
        this.numberEntries = numberEntries;
    }
}
