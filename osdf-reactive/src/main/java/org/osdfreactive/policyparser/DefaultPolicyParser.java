/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osdfreactive.policyparser;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onlab.util.Tools;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.packet.InboundPacket;
import org.osdfreactive.applicationinfo.ApplicationTypes;
import org.osdfreactive.policies.DefaultPolicy;
import org.osdfreactive.trafficprofiles.DefaultTrafficProfile;
import org.osdfreactive.transportprotocolinfo.TransportProtocols;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * A Default implementation of policy parser for policies inside a region.
 */

@Component(immediate = true)
@Service
public class DefaultPolicyParser
        extends AbstractPolicyParser
        implements PolicyParserInterface {


    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_PRIORITY = 10;

    private final Logger log = getLogger(getClass());
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HttpPolicyPaserInterface httpPolicyPaser;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IcmpPolicyParserInterface icmpPolicyParser;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VstreamingPolicyParserInterface vStreamingPolicyParser;

    @Property(name = "packetOutOnly", boolValue = false,
            label = "Enable packet-out only forwarding; default is false")
    private boolean packetOutOnly = false;

    @Property(name = "packetOutOfppTable", boolValue = false,
            label = "Enable first packet forwarding using OFPP_TABLE port " +
                    "instead of PacketOut with actual port; default is false")
    private boolean packetOutOfppTable = false;

    @Property(name = "flowTimeout", intValue = DEFAULT_TIMEOUT,
            label = "Configure Flow Timeout for installed flow rules; " +
                    "default is 10 sec")
    private int flowTimeout = DEFAULT_TIMEOUT;

    @Property(name = "flowPriority", intValue = DEFAULT_PRIORITY,
            label = "Configure Flow Priority for installed flow rules; " +
                    "default is 10")
    private int flowPriority = DEFAULT_PRIORITY;

    @Property(name = "ipv6Forwarding", boolValue = false,
            label = "Enable IPv6 forwarding; default is false")
    private boolean ipv6Forwarding = false;

    @Property(name = "matchDstMacOnly", boolValue = false,
            label = "Enable matching Dst Mac Only; default is false")
    private boolean matchDstMacOnly = false;

    @Property(name = "matchVlanId", boolValue = false,
            label = "Enable matching Vlan ID; default is false")
    private boolean matchVlanId = false;

    @Property(name = "matchIpv4Address", boolValue = true,
            label = "Enable matching IPv4 Addresses; default is false")
    private boolean matchIpv4Address = true;

    @Property(name = "matchIpv4Dscp", boolValue = false,
            label = "Enable matching IPv4 DSCP and ECN; default is false")
    private boolean matchIpv4Dscp = false;

    @Property(name = "matchIpv6Address", boolValue = false,
            label = "Enable matching IPv6 Addresses; default is false")
    private boolean matchIpv6Address = false;

    @Property(name = "matchIpv6FlowLabel", boolValue = false,
            label = "Enable matching IPv6 FlowLabel; default is false")
    private boolean matchIpv6FlowLabel = false;

    @Property(name = "matchTcpUdpPorts", boolValue = true,
            label = "Enable matching TCP/UDP ports; default is false")
    private boolean matchTcpUdpPorts = true;

    @Property(name = "matchIcmpFields", boolValue = true,
            label = "Enable matching ICMPv4 and ICMPv6 fields; " +
                    "default is false")
    private boolean matchIcmpFields = true;


    @Property(name = "ignoreIPv4Multicast", boolValue = false,
            label = "Ignore (do not forward) IPv4 multicast packets; default is false")
    private boolean ignoreIpv4McastPackets = false;

    @Property(name = "recordMetrics", boolValue = false,
            label = "Enable record metrics for reactive forwarding")
    private boolean recordMetrics = false;


    @Activate
    public void activate(ComponentContext context) {
        log.info("Started");


        selectorBuilder = DefaultTrafficSelector.builder();
        readComponentConfiguration(context);

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");

    }

    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);

    }

    /**
     * Extracts properties from the component configuration context.
     *
     * @param context the component context
     */
    private void readComponentConfiguration(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        Boolean packetOutOnlyEnabled =
                Tools.isPropertyEnabled(properties, "packetOutOnly");
        if (packetOutOnlyEnabled == null) {
            log.info("Packet-out is not configured, " + "using current value of {}" + packetOutOnly);
        } else {
            packetOutOnly = packetOutOnlyEnabled;
            log.info("Configured. Packet-out only forwarding is {}",
                    packetOutOnly ? "enabled" : "disabled");
        }

        Boolean packetOutOfppTableEnabled =
                Tools.isPropertyEnabled(properties, "packetOutOfppTable");
        if (packetOutOfppTableEnabled == null) {
            log.info("OFPP_TABLE port is not configured, " +
                    "using current value of {}", packetOutOfppTable);
        } else {
            packetOutOfppTable = packetOutOfppTableEnabled;
            log.info("Configured. Forwarding using OFPP_TABLE port is {}",
                    packetOutOfppTable ? "enabled" : "disabled");
        }

        Boolean ipv6ForwardingEnabled =
                Tools.isPropertyEnabled(properties, "ipv6Forwarding");
        if (ipv6ForwardingEnabled == null) {
            log.info("IPv6 forwarding is not configured, " +
                    "using current value of {}", ipv6Forwarding);
        } else {
            ipv6Forwarding = ipv6ForwardingEnabled;
            log.info("Configured. IPv6 forwarding is {}",
                    ipv6Forwarding ? "enabled" : "disabled");
        }

        Boolean matchDstMacOnlyEnabled =
                Tools.isPropertyEnabled(properties, "matchDstMacOnly");
        if (matchDstMacOnlyEnabled == null) {
            log.info("Match Dst MAC is not configured, " +
                    "using current value of {}", matchDstMacOnly);
        } else {
            matchDstMacOnly = matchDstMacOnlyEnabled;
            log.info("Configured. Match Dst MAC Only is {}",
                    matchDstMacOnly ? "enabled" : "disabled");
        }

        Boolean matchVlanIdEnabled =
                Tools.isPropertyEnabled(properties, "matchVlanId");
        if (matchVlanIdEnabled == null) {
            log.info("Matching Vlan ID is not configured, " +
                    "using current value of {}", matchVlanId);
        } else {
            matchVlanId = matchVlanIdEnabled;
            log.info("Configured. Matching Vlan ID is {}",
                    matchVlanId ? "enabled" : "disabled");
        }

        Boolean matchIpv4AddressEnabled =
                Tools.isPropertyEnabled(properties, "matchIpv4Address");
        if (matchIpv4AddressEnabled == null) {
            log.info("Matching IPv4 Address is not configured, " +
                    "using current value of {}", matchIpv4Address);
        } else {
            matchIpv4Address = matchIpv4AddressEnabled;
            log.info("Configured. Matching IPv4 Addresses is {}",
                    matchIpv4Address ? "enabled" : "disabled");
        }

        Boolean matchIpv4DscpEnabled =
                Tools.isPropertyEnabled(properties, "matchIpv4Dscp");
        if (matchIpv4DscpEnabled == null) {
            log.info("Matching IPv4 DSCP and ECN is not configured, " +
                    "using current value of {}", matchIpv4Dscp);
        } else {
            matchIpv4Dscp = matchIpv4DscpEnabled;
            log.info("Configured. Matching IPv4 DSCP and ECN is {}",
                    matchIpv4Dscp ? "enabled" : "disabled");
        }

        Boolean matchIpv6AddressEnabled =
                Tools.isPropertyEnabled(properties, "matchIpv6Address");
        if (matchIpv6AddressEnabled == null) {
            log.info("Matching IPv6 Address is not configured, " +
                    "using current value of {}", matchIpv6Address);
        } else {
            matchIpv6Address = matchIpv6AddressEnabled;
            log.info("Configured. Matching IPv6 Addresses is {}",
                    matchIpv6Address ? "enabled" : "disabled");
        }

        Boolean matchIpv6FlowLabelEnabled =
                Tools.isPropertyEnabled(properties, "matchIpv6FlowLabel");
        if (matchIpv6FlowLabelEnabled == null) {
            log.info("Matching IPv6 FlowLabel is not configured, " +
                    "using current value of {}", matchIpv6FlowLabel);
        } else {
            matchIpv6FlowLabel = matchIpv6FlowLabelEnabled;
            log.info("Configured. Matching IPv6 FlowLabel is {}",
                    matchIpv6FlowLabel ? "enabled" : "disabled");
        }

        Boolean matchTcpUdpPortsEnabled =
                Tools.isPropertyEnabled(properties, "matchTcpUdpPorts");
        if (matchTcpUdpPortsEnabled == null) {
            log.info("Matching TCP/UDP fields is not configured, " +
                    "using current value of {}", matchTcpUdpPorts);
        } else {
            matchTcpUdpPorts = matchTcpUdpPortsEnabled;
            log.info("Configured. Matching TCP/UDP fields is {}",
                    matchTcpUdpPorts ? "enabled" : "disabled");
        }

        Boolean matchIcmpFieldsEnabled =
                Tools.isPropertyEnabled(properties, "matchIcmpFields");
        if (matchIcmpFieldsEnabled == null) {
            log.info("Matching ICMP (v4 and v6) fields is not configured, " +
                    "using current value of {}", matchIcmpFields);
        } else {
            matchIcmpFields = matchIcmpFieldsEnabled;
            log.info("Configured. Matching ICMP (v4 and v6) fields is {}",
                    matchIcmpFields ? "enabled" : "disabled");
        }

        Boolean ignoreIpv4McastPacketsEnabled =
                Tools.isPropertyEnabled(properties, "ignoreIpv4McastPackets");
        if (ignoreIpv4McastPacketsEnabled == null) {
            log.info("Ignore IPv4 multi-cast packet is not configured, " +
                    "using current value of {}", ignoreIpv4McastPackets);
        } else {
            ignoreIpv4McastPackets = ignoreIpv4McastPacketsEnabled;
            log.info("Configured. Ignore IPv4 multicast packets is {}",
                    ignoreIpv4McastPackets ? "enabled" : "disabled");
        }
        Boolean recordMetricsEnabled =
                Tools.isPropertyEnabled(properties, "recordMetrics");
        if (recordMetricsEnabled == null) {
            log.info("IConfigured. Ignore record metrics  is {} ," +
                    "using current value of {}", recordMetrics);
        } else {
            recordMetrics = recordMetricsEnabled;
            log.info("Configured. record metrics  is {}",
                    recordMetrics ? "enabled" : "disabled");
        }

        flowTimeout = Tools.getIntegerProperty(properties, "flowTimeout", DEFAULT_TIMEOUT);
        log.info("Configured. Flow Timeout is configured to {} seconds", flowTimeout);

        flowPriority = Tools.getIntegerProperty(properties, "flowPriority", DEFAULT_PRIORITY);
        log.info("Configured. Flow Priority is configured to {}", flowPriority);
    }


    private TrafficSelector.Builder localInterTrafficSelector(InboundPacket pkt,
                                                              Ethernet ethPkt,
                                                              MacAddress dstMac,
                                                              DefaultPolicy policy) {
        DefaultTrafficProfile trafficProfile = policy.getTrafficProfile();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

        HostId dstId = HostId.hostId(ethPkt.getDestinationMAC());
        HostId srcId = HostId.hostId(ethPkt.getSourceMAC());
        TransportProtocols transportProtocol = trafficProfile.getApplicationType().getTransportProtocol();

        if (TransportProtocols.TCPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.WEB) {
            return httpPolicyPaser.interHttpTrafficSelector(pkt, ethPkt, dstMac, policy);


        } else if (TransportProtocols.ICMPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.PING) {
            return icmpPolicyParser.interPingTrafficSelector(pkt, ethPkt, dstMac, policy);
        } else if (TransportProtocols.TCPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.VIDEO_STREAMING) {
            return vStreamingPolicyParser.interVideoStreamTrafficSelector(pkt, ethPkt, policy);
        }


        return null;
    }

    private TrafficSelector.Builder localIntraTrafficSelector(InboundPacket pkt,
                                                              Ethernet ethPkt,
                                                              Link pathLink,
                                                              DefaultPolicy policy) {
        DefaultTrafficProfile trafficProfile = policy.getTrafficProfile();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

        HostId dstId = HostId.hostId(ethPkt.getDestinationMAC());
        HostId srcId = HostId.hostId(ethPkt.getSourceMAC());
        TransportProtocols transportProtocol = trafficProfile.getApplicationType().getTransportProtocol();

        if (TransportProtocols.TCPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.WEB) {
            return httpPolicyPaser.intraHttpTrafficSelector(pkt, ethPkt, policy);

        } else if (TransportProtocols.UDPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.VOIP) {
            return voipTrafficSelector(pkt, ethPkt, policy);

        } else if (TransportProtocols.ICMPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.PING) {
            return icmpPolicyParser.intraPingTrafficSelector(pkt, ethPkt, pathLink, policy);

        } else if (TransportProtocols.TCPv4 == transportProtocol
                && policy.getTrafficProfile().getApplicationType() == ApplicationTypes.VIDEO_STREAMING) {

            return vStreamingPolicyParser.intraVideoStreamTrafficSelector(pkt, ethPkt, policy);

        }


        return null;


    }

    private TrafficSelector.Builder voipTrafficSelector(InboundPacket pkt,
                                                        Ethernet ethPkt,
                                                        DefaultPolicy policy) {

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

        byte ipv4Protocol = ipv4Packet.getProtocol();
        if (ipv4Protocol != IPv4.PROTOCOL_UDP
                || ethPkt.getEtherType() != Ethernet.TYPE_IPV4) {
            return null;
        }


        if (matchIpv4Address
                && ethPkt.getEtherType() == Ethernet.TYPE_IPV4
                && ipv4Packet.getProtocol() == IPv4.PROTOCOL_UDP) {

            Ip4Prefix matchIp4SrcPrefix =
                    Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                            Ip4Prefix.MAX_MASK_LENGTH);
            Ip4Prefix matchIp4DstPrefix =
                    Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                            Ip4Prefix.MAX_MASK_LENGTH);
            selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                    .matchIPSrc(matchIp4SrcPrefix)
                    .matchIPDst(matchIp4DstPrefix);


            selectorBuilder.matchEthSrc(ethPkt.getSourceMAC());
            selectorBuilder.matchEthDst(ethPkt.getDestinationMAC());


        }
        if (matchTcpUdpPorts
                && ipv4Protocol == IPv4.PROTOCOL_UDP) {
            UDP udpPacket = (UDP) ipv4Packet.getPayload();
            selectorBuilder.matchIPProtocol(ipv4Protocol)
                    .matchUdpSrc(TpPort.tpPort((udpPacket.getSourcePort())))
                    .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()))
                    .matchIPProtocol(IPv4.PROTOCOL_UDP);
        }

        return selectorBuilder;
    }


    private TrafficSelector.Builder interBuildTrafficSelectorWH(InboundPacket pkt,
                                                                Ethernet ethPkt,
                                                                MacAddress dstMac,
                                                                DefaultPolicy policy) {
        return localInterTrafficSelector(pkt, ethPkt, dstMac, policy);
    }

    private TrafficSelector.Builder interBuildTrafficSelectorWithHosts(InboundPacket pkt,
                                                                       Ethernet ethPkt,
                                                                       MacAddress dstMac,
                                                                       DefaultPolicy policy) {

        List<HostId> srcHostsList = new ArrayList<>();
        List<HostId> dstHostsList = new ArrayList<>();
        srcHostsList = policy.getSrcHostsList();
        dstHostsList = policy.getDstHostsList();

        HostId dstId = HostId.hostId(dstMac);
        HostId srcId = HostId.hostId(ethPkt.getSourceMAC());


        if ((srcHostsList.contains(srcId) && dstHostsList.contains(dstId)) ||
                (srcHostsList.contains(dstId) && dstHostsList.contains(srcId))) {

            return localInterTrafficSelector(pkt, ethPkt, dstMac, policy);

        } else if (srcHostsList.isEmpty() && (dstHostsList.contains(dstId) || dstHostsList.contains(srcId))) {
            return localInterTrafficSelector(pkt, ethPkt, dstMac, policy);

        } else if (dstHostsList.isEmpty() && (srcHostsList.contains(srcId) || srcHostsList.contains(dstId))) {
            return localInterTrafficSelector(pkt, ethPkt, dstMac, policy);
        } else {

            return null;
        }

    }


    private TrafficSelector.Builder intraBuildTrafficSelectorWH(InboundPacket pkt,
                                                                Ethernet ethPkt,
                                                                Link pathLink,
                                                                DefaultPolicy policy) {


        return localIntraTrafficSelector(pkt, ethPkt, pathLink, policy);
    }


    private TrafficSelector.Builder intraBuildTrafficSelectorWithHosts(InboundPacket pkt,
                                                                       Ethernet ethPkt,
                                                                       Link pathLink,
                                                                       DefaultPolicy policy) {

        List<HostId> srcHostsList = new ArrayList<>();
        List<HostId> dstHostsList = new ArrayList<>();
        srcHostsList = policy.getSrcHostsList();
        dstHostsList = policy.getDstHostsList();

        HostId dstId = HostId.hostId(ethPkt.getDestinationMAC());
        HostId srcId = HostId.hostId(ethPkt.getSourceMAC());


        if ((srcHostsList.contains(srcId) && dstHostsList.contains(dstId)) ||
                (srcHostsList.contains(dstId) && dstHostsList.contains(srcId))) {


            return localIntraTrafficSelector(pkt, ethPkt, pathLink, policy);

        } else if (srcHostsList.isEmpty() && (dstHostsList.contains(dstId) || dstHostsList.contains(srcId))) {
            return localIntraTrafficSelector(pkt, ethPkt, pathLink, policy);

        } else if (dstHostsList.isEmpty() && (srcHostsList.contains(srcId) || srcHostsList.contains(dstId))) {
            return localIntraTrafficSelector(pkt, ethPkt, pathLink, policy);
        } else {

            return null;
        }

    }

    /**
     * Build a traffic selector based on given policy and incoming packets.
     *
     * @param pkt      Incoming packet
     * @param ethPkt   Ethernet packet
     * @param pathLink pathlink
     * @param policy   given network policy
     * @return a traffic selector
     */
    public TrafficSelector.Builder intraBuildTrafficSelector(InboundPacket pkt,
                                                             Ethernet ethPkt,
                                                             Link pathLink,
                                                             DefaultPolicy policy) {


        List<HostId> srcHostsList = new ArrayList<>();
        List<HostId> dstHostsList = new ArrayList<>();
        srcHostsList = policy.getSrcHostsList();
        dstHostsList = policy.getDstHostsList();


        if (srcHostsList.isEmpty() && dstHostsList.isEmpty()) {

            return intraBuildTrafficSelectorWH(pkt, ethPkt, pathLink, policy);
        } else {
            return intraBuildTrafficSelectorWithHosts(pkt, ethPkt, pathLink, policy);
        }


    }

    @Override
    public TrafficSelector.Builder interBuildTrafficSelector(InboundPacket pkt,
                                                             Ethernet ethPkt,
                                                             MacAddress dstMac,
                                                             DefaultPolicy policy) {

        List<HostId> srcHostsList = new ArrayList<>();
        List<HostId> dstHostsList = new ArrayList<>();
        srcHostsList = policy.getSrcHostsList();
        dstHostsList = policy.getDstHostsList();

        if (srcHostsList.isEmpty() && dstHostsList.isEmpty()) {

            return interBuildTrafficSelectorWH(pkt, ethPkt, dstMac, policy);
        } else {
            log.info("Inter build traffic selector with hosts");
            return interBuildTrafficSelectorWithHosts(pkt, ethPkt, dstMac, policy);
        }

    }


}
