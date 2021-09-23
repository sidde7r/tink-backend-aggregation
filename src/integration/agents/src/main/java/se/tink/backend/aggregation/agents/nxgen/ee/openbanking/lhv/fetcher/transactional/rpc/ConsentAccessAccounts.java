package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessAccounts {
    private String iban;

    public ConsentAccessAccounts(String iban) {
        this.iban = iban;
    }
}
