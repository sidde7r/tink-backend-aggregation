package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ConsentRequest extends ConsentBaseRequest {

    private final AccessEntity access = new AccessEntity();

    public ConsentRequest() {}

    @Override
    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public AccessEntity getAccess() {
        return access;
    }
}
