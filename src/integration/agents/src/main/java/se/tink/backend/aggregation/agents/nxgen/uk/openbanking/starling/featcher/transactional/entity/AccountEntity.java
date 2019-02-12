package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    private String accountUid;
    private String defaultCategory;
    private String currency;
    private String createdAt;

    public String getAccountUid() {
        return accountUid;
    }
}
