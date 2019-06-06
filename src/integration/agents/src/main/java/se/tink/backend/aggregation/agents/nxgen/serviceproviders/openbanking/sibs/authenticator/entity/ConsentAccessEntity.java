package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessEntity {

    // private List<ConsentPayloadEntity> accounts = null;
    // private List<ConsentPayloadEntity> balances = null;
    // private List<ConsentPayloadEntity> transactions = null;
    // private String availableAccounts;
    private String allPsd2;

    public ConsentAccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
