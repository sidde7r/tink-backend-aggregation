package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountBalanceEntity {

    private String accountId;
    private AmountEntity current;
    private AmountEntity available;
    private String status;
}
