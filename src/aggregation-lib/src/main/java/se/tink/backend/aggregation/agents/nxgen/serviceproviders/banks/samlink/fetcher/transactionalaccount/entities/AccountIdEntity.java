package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntity {
    private String iban;
    private String bic;

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }
}
