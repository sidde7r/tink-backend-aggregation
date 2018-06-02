package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractPayeeEntity {
    protected String name;
    protected String id;
    protected String type;
    protected String accountNumber;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
