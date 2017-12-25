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

import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onosproject.net.Link;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.packet.InboundPacket;
import org.osdfreactive.policies.DefaultPolicy;

/**
 * Policy Parser interface.
 */
public interface PolicyParserInterface {

    /**
     * General intra-domain traffic selector builder based on a given policy.
     *
     * @param pkt    Inbound packet
     * @param ethPkt Ethernet packet
     * @param link
     * @param policy a policy
     * @return Traffic selector builder
     */
    TrafficSelector.Builder intraBuildTrafficSelector(InboundPacket pkt,
                                                      Ethernet ethPkt,
                                                      Link link, DefaultPolicy policy);


    /**
     * General inter-domain traffic selector builder based on a given policy.
     *
     * @param pkt    Inbound packet
     * @param ethPkt Ethernet packet
     * @param dstMac
     * @param policy a policy
     * @return Traffic selector builder
     */
    TrafficSelector.Builder interBuildTrafficSelector(InboundPacket pkt,
                                                      Ethernet ethPkt,
                                                      MacAddress dstMac,
                                                      DefaultPolicy policy);
}
