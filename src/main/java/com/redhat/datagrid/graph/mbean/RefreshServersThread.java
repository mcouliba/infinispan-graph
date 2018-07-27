package com.redhat.datagrid.graph.mbean;

import com.redhat.datagrid.graph.service.ClusterInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mcouliba on 25/07/2018.
 */
public class RefreshServersThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshServersThread.class);

    private static final long DEFAULT_REFRESH_RATE = 2000L;

    private volatile boolean running;

    private final ClusterInfoService clusterInfoService;

    public RefreshServersThread(ClusterInfoService clusterInfoService) {
        this.clusterInfoService = clusterInfoService;
        this.setDaemon(false);
    }

    public void abort() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                clusterInfoService.updateMBeanServers();
            } catch (Exception e) {
                LOGGER.error("Exception while updating MBeanServers", e);
            }
            try {
                Thread.sleep(DEFAULT_REFRESH_RATE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
