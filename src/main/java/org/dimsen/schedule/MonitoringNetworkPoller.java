package org.dimsen.schedule;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.dimsen.service.MonitoringNetworkService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
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

    @ConfigProperty(name = "oid.to.track")
    String oidToTrack;

    @ConfigProperty(name = "oid.to.track.in")
    String oidToTrackIn;

    @ConfigProperty(name = "oid.to.track.out")
    String oidToTrackOut;

//    @Scheduled(every = "10s")
//    public void pollDevice() {
//        for (String ip : ips) {
//            try {
//                String result = monitoringNetworkService.getSysDesc(ip, oidToTrack);
//                System.out.println("SNMP OID result: " + result);
//            } catch (Exception e) {
//                System.out.println("Error while getting device info: " + ip + " Error: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }

    // Store previous values: Map<ip, Pair<in, out>>
    private final Map<String, long[]> previousOctetsMap = new HashMap<>();

    @Scheduled(every = "10s")
    public void pollDevice() {
        int pollingIntervalSeconds = 10;

        for (String ip : ips) {
            try {
                // Get current octets (as String, must convert to long)
                String resultOctetInStr = monitoringNetworkService.getSysDesc(ip, oidToTrackIn);
                String resultOctetOutStr = monitoringNetworkService.getSysDesc(ip, oidToTrackOut);

                long currentIn = Long.parseLong(resultOctetInStr);
                long currentOut = Long.parseLong(resultOctetOutStr);

                // Get previous values
                long[] previous = previousOctetsMap.get(ip);
                if (previous != null) {
                    long prevIn = previous[0];
                    long prevOut = previous[1];

                    // Calculate differences
                    long deltaInBytes = currentIn - prevIn;
                    long deltaOutBytes = currentOut - prevOut;

                    // Handle counter reset (wrap around 32-bit counter)
                    if (deltaInBytes < 0) deltaInBytes += 4294967296L; // 2^32
                    if (deltaOutBytes < 0) deltaOutBytes += 4294967296L;

                    // Convert bytes to bits per second
                    long inBps = (deltaInBytes * 8) / pollingIntervalSeconds;
                    long outBps = (deltaOutBytes * 8) / pollingIntervalSeconds;

                    System.out.println("IP: " + ip);
                    System.out.println("Upload (out): " + outBps + " bps");
                    System.out.println("Download (in): " + inBps + " bps");
                }

                // Update previous value
                previousOctetsMap.put(ip, new long[]{currentIn, currentOut});

            } catch (Exception e) {
                System.out.println("Error while getting device info: " + ip + " Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

//    @Scheduled(every = "30s")
//    public void walkItThrough() {
//        try {
//            Map<String, String> snmpWalk = monitoringNetworkService.snmpWalk(ips.getFirst(), startoid);
////            System.out.println("SNMP result: " + snmpWalk);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
