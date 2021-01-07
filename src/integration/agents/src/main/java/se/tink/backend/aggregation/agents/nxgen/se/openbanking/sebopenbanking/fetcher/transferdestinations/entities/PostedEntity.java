package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PostedEntity {
    private String interestCapitalizationAccountId;
    private String interestCapitalizationAmount;
    private String interestCapitalizationPostingDate;
    private String interestType;
    private String pdAccountability;
    private String transactionAccountId;
}
