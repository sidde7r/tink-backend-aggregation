package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExtendedDateEntity {
    private String terminal;
    private String userCode;
    private String bankCode;

    public String getTerminal() {
        return terminal;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getBankCode() {
        return bankCode;
    }
}
