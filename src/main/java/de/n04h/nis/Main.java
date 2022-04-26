package de.n04h.nis;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            ServiceInfo svi = jmdns.getServiceInfo("_nisserver", "nis-main");

            if(svi != null){
                ServiceInfo serviceInfo = ServiceInfo.create("_nisserver", "nis-main", 1234, "");
                jmdns.registerService(serviceInfo);

                Thread.sleep(25000);

                jmdns.unregisterAllServices();
            }else{
                
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
