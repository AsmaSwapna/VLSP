package usr.common;

import usr.localcontroller.LocalControllerInfo;

/**
Class provides simple information that the global and local
controllers need about a router
*/
public class BasicRouterInfo {
    private long startTime_;
    private LocalControllerInfo controller_;
    private int managementPort_;
    private int routingPort_;
    private int routerId_;
    
    public BasicRouterInfo(int id, long time, LocalControllerInfo lc, int port1) {
         this(id,time,lc,port1,port1+1);
    }
    public BasicRouterInfo(int id, long time, LocalControllerInfo lc, int port1, 
      int port2) {
        startTime_= time;
        controller_= lc;
        managementPort_= port1;
        routingPort_= port2;
    }
    
    public int getId() {
        return routerId_;
    }
    
    public int getManagementPort() {
        return managementPort_;
    }
    
    public int getRoutingPort() {
        return routingPort_;
    }
    
    public LocalControllerInfo getLocalControllerInfo() {
        return controller_;
    }
    
    public String getHost() {
        return controller_.getName();
    }
}