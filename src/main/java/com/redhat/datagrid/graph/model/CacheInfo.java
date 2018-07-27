package com.redhat.datagrid.graph.model;


import java.net.SocketAddress;

/**
 * Created by mcouliba on 23/07/2018.
 */
public class CacheInfo {

    String name;

    String container;

    protected CacheInfo() {
    }

    public CacheInfo(String name, String container) {
        this.name = name;
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

}
