package se.tink.backend.aggregation.agents.banks;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.HandelsbankenV6Agent;
import se.tink.backend.common.config.SignatureKeyPair;

public class HandelsbankenAgent extends HandelsbankenV6Agent {
    public HandelsbankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
