package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalanceEntity {
    private Double amount;
    private String currencyCode;
}
