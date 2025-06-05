package org.dimsen.schedule;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.dimsen.service.MonitoringNetworkService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

@Singleton
public class MonitoringNetworkPoller {

    @Inject
    MonitoringNetworkService monitoringNetworkService;

    @ConfigProperty(name = "snmp.target.ips")
    List<String> ips;

    @ConfigProperty(name = "startoid")
    String startoid;

//    @Scheduled(every = "10s")
//    public void pollDevice() {
//        for (String ip : ips) {
//            try {
//                String result = monitoringNetworkService.getSysDesc(ip);
//                System.out.println("SNMP result: " + result);
//            } catch (Exception e) {
//                System.out.println("Error while getting device info: " + ip + " Error: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }

    @Scheduled(every = "30s")
    public void walkItThrough() {
        try {
            Map<String, String> snmpWalk = monitoringNetworkService.snmpWalk(ips.getFirst(), startoid);
            System.out.println("SNMP result: " + snmpWalk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
