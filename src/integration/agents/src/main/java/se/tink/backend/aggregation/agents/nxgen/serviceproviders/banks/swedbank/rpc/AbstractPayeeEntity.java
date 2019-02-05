package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractPayeeEntity extends AbstractAccountEntity {
    protected String type;

    public String getType() {
        return type;
    }
}
