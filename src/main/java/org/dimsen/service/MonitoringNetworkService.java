package org.dimsen.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.dimsen.model.SnmpEntry;
import org.dimsen.repository.SnmpRepository;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@ApplicationScoped
public class MonitoringNetworkService {

    @Inject
    SnmpRepository snmpRepository;

//    public String getSysDesc(String ip) throws Exception {
//        Address targetAddress = GenericAddress.parse("udp:" + ip + "/161");
//        TransportMapping<UdpAddress>  transport = new DefaultUdpTransportMapping();
//        Snmp snmp = new Snmp(transport);
//        transport.listen();
//
//        CommunityTarget target = new CommunityTarget();
//        target.setCommunity(new OctetString("public"));
//        target.setAddress(targetAddress);
//        target.setRetries(2);
//        target.setTimeout(10000);
//        target.setVersion(SnmpConstants.version2c);
//
//        PDU pdu = new PDU();
//        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.2.17"))); // ifDescr.17
//        pdu.setType(PDU.GET);
//
//        ResponseEvent response = snmp.send(pdu, target);
//        snmp.close();
//
//        if (response.getResponse() != null) {
//            return response.getResponse().get(0).getVariable().toString();
//        } else  {
//            return "No response";
//        }
//    }

    public Map<String, String> snmpWalk(String ip, String startOid) throws IOException {
        Map<String, String> result = new HashMap<>();
        Address targetAddress = GenericAddress.parse("udp:" + ip + "/161");
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        OID rootOID = new OID(startOid);
        OID currentOID = rootOID;

        while (true) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(currentOID));
            pdu.setType(PDU.GETNEXT);

            ResponseEvent event = snmp.getNext(pdu, target);
            PDU response = event.getResponse();

            if (response == null || response.getVariableBindings().isEmpty()) {
                System.out.println("No response or empty variable bindings");
                break;
            }

            VariableBinding vb = response.get(0);
            if (vb.getOid() == null || vb.getOid().size() < rootOID.size()
                    || !vb.getOid().startsWith(rootOID)
                    || vb.getOid().equals(currentOID)) {
                System.out.println("Reached end of subtree or invalid/duplicate OID");
                break;
            }

            result.put(vb.getOid().toString(), vb.getVariable().toString());

            currentOID = vb.getOid();
        }

        SnmpEntry snmpEntry = SnmpEntry.builder()   
                .oid(result.keySet().toString())
                .value(result.values().toString())
                .build();

        snmpRepository.saveData(snmpEntry);

        snmp.close();
        return result;
    }
}
