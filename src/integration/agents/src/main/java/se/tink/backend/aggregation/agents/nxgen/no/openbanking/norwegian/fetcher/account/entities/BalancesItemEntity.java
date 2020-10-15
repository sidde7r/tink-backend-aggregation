package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {

    private String balanceType;

    @JsonProperty("balanceAmount")
    @Getter
    private AmountEntity amountEntity;

    boolean isExpectedBalance() {
        return NorwegianConstants.ResponseValues.BALANCE_TYPE_EXPECTED.equalsIgnoreCase(
                balanceType);
    }

    boolean isClosingBalance() {
        return NorwegianConstants.ResponseValues.BALANCE_TYPE_CLOSING.equalsIgnoreCase(balanceType);
    }
}
