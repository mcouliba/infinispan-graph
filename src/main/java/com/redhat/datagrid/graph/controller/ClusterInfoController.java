package com.redhat.datagrid.graph.controller;

import com.redhat.datagrid.graph.model.CacheInfo;
import com.redhat.datagrid.graph.model.NodeInfo;
import com.redhat.datagrid.graph.service.ClusterInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
//specifying endpoint location
@RequestMapping("/clusterinfo")
public class ClusterInfoController {

    @Autowired
    private ClusterInfoService clusterInfoService;

    @Value("${infinispan.graph.demo}")
    private boolean isDemo;

    @RequestMapping(method=RequestMethod.GET, value = "/nodes")
    public List<NodeInfo> getNodeInfo(@RequestParam("cacheName") String cacheName
            , @RequestParam("containerName") String containerName) throws Exception {
        List<NodeInfo> response;

        if (isDemo) {
            response = clusterInfoService.getDemoNodeInfo();
        } else {
            clusterInfoService.updateMBeanServers(); // TODO Create and internal thread to update Server lists
            response = clusterInfoService.getNodeInfo(cacheName, containerName);
        }

        return response;
    }

    @RequestMapping(method=RequestMethod.GET, value = "/caches")
    public List<CacheInfo> getCacheInfo() throws Exception {
        List<CacheInfo> response;

        if (isDemo) {
            response = clusterInfoService.getDemoCacheInfo();
        } else {
            clusterInfoService.updateMBeanServers(); // TODO Create and internal thread to update Server lists
            response = clusterInfoService.getCacheInfo();
        }
        return response;
    }
}