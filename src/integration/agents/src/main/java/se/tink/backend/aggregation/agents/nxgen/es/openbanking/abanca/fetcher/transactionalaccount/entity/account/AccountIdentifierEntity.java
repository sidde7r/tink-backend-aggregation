package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdentifierEntity {

    private String bic;
    private String number; // Iban
    private String type;

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }
}
