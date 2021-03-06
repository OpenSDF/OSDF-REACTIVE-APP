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

package org.osdfreactive.policystorage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.store.AbstractStore;
import org.osdfreactive.policies.DefaultPolicyId;
import org.osdfreactive.policies.Policy;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * A simple implementation of policy store.
 */

@Component(immediate = true)
@Service
public class SimplePolicyStore
        extends
        AbstractStore<PolicyEvent, PolicyStoreDelegate>
        implements PolicyStore {


    private final Multimap<DefaultPolicyId, DefaultFlowRule> flowRuleList = ArrayListMultimap.create();
    private final Logger log = getLogger(getClass());
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;
    private Map<DefaultPolicyId, Policy> current = Maps.newConcurrentMap();

    @Activate
    public void activate() {
        log.info("Started");

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public Iterable<Policy> getPolicies() {
        return current.values();
    }


    @Override
    public Iterable<Policy> getCurrentPolicies() {
        return current.values();
    }


    /**
     * Add a policy into the current list of policies.
     *
     * @param policy a policy
     */
    @Override
    public void addCurrent(Policy policy) {


        current.put((DefaultPolicyId) policy.getPolicyId(), policy);

        PolicyEvent.getEvent(policy).ifPresent(this::notifyDelegate);

    }

    /**
     * Add a flow rule to the list of flow rules for a given policy.
     *
     * @param policy   policy
     * @param flowRule flow rule
     */
    @Override
    public void addFlowRule(Policy policy, DefaultFlowRule flowRule) {

        flowRuleList.put((DefaultPolicyId) policy.getPolicyId(), flowRule);
    }


    /**
     * Remove a policy from list of current active polices.
     *
     * @param policy a policy
     */
    @Override
    public void removeCurrentPolicy(Policy policy) {


        Collection<DefaultFlowRule> flowRules = flowRuleList.get((DefaultPolicyId) policy.getPolicyId());
        for (FlowRule flowRule : flowRules) {
            flowRuleService.removeFlowRules(flowRule);
        }
        current.remove(policy.getPolicyId());

        //PolicyEvent.getEvent(policy).ifPresent(this::notifyDelegate);
    }

    /**
     * Return a policy based on a given policyId.
     *
     * @param policyId policy ID.
     * @return a policy.
     */
    @Override
    public Policy getPolicy(String policyId) {

        for (Policy policy : current.values()) {
            if (policy.getPolicyId().getPolicyId() == Integer.parseInt(policyId)) {
                return policy;
            }
        }
        return null;
    }

    /**
     * Return a list of current active polices in the system.
     *
     * @return a map of active policies.
     */
    @Override
    public Map<DefaultPolicyId, Policy> getCurrentPolicyMap() {
        return current;
    }

    @Override
    public int getRulesCount(Policy policy) {
        return flowRuleList.get((DefaultPolicyId) policy.getPolicyId()).size();

    }

    /**
     * Return the flow rules list.
     *
     * @return flow rule list
     */
    @Override
    public Multimap<DefaultPolicyId, DefaultFlowRule> getFlowRulesList() {

        return flowRuleList;
    }

    @Override
    public Collection<DefaultFlowRule> getFlowRulesForPolicy(Policy policy) {
        return flowRuleList.get((DefaultPolicyId) policy.getPolicyId());
    }


}
