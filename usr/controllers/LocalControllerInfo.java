/**
  * LocalHostInfo contains basic info about one host in the system
  * It deals with finding out IP addresses, ports and so on
*/

package usr.controllers;
import usr.common.LocalHostInfo;

public class LocalControllerInfo extends LocalHostInfo {
    private int maxRouters_= 100;
    private int currRouters_= 0;
    private String remoteLoginUser_ = null;
    private String remoteStartController_ = null;
    
    public LocalControllerInfo(String hostName, int port) 
    {
       super(hostName,port);
    }
    
    public LocalControllerInfo(java.net.InetAddress ip, int port) 
    {
       super(ip,port);
    }
    
    public LocalControllerInfo(int port) 
    {
       super(port);
    }
    
    public int getMaxRouters() {
       return maxRouters_;
    }
    
    public void addRouter() {
        currRouters_++;
    }
    
    public void delRouter() {
        currRouters_--;
    }
    
    public double getUsage() {
        double usage= currRouters_/maxRouters_;
        return usage;
    }
    
    public void setMaxRouters(int maxR) {
       maxRouters_= maxR;
    }
    
    public void setRemoteLoginUser(String u) {
        remoteLoginUser_= u;
    }
    
    public String getRemoteLoginUser() {
        return remoteLoginUser_;
    }
    
    public void setRemoteStartController(String s) {
        remoteStartController_= s;
    }
    
    public String getRemoteStartController() {
        return remoteStartController_;
    }
    
}
