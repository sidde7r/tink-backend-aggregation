package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.rpcs;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentRequest;

public class BawagConsentRequest extends ConsentRequest {
    public BawagConsentRequest(int frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }
}
