package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import se.tink.backend.agents.rpc.Credentials;

public interface SupplementalInformationRequester {
    void requestSupplementalInformation(String mfaId, Credentials credentials);
}
