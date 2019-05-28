package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ConsentRequest extends ConsentBaseRequest {

    private final AccessEntity access = new AccessEntity();

    public ConsentRequest(final int frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public AccessEntity getAccess() {
        return access;
    }
}
