package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankEntity {
    private String name;
    private String bic;
    private String country;

    public String getName() {
        return name;
    }

    public String getBic() {
        return bic;
    }

    public String getCountry() {
        return country;
    }
}
