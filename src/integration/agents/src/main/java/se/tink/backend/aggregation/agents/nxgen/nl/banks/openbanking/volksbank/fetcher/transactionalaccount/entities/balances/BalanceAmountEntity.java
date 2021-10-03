package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.balances;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalanceAmountEntity {

    private String amount;
    private String currency;
}
