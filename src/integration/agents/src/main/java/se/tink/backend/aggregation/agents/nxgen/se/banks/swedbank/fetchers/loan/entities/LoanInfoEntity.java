package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanInfoEntity {
    private String name;
    private AccountEntity account;
    private AccountEntity accountForPayment;
}
