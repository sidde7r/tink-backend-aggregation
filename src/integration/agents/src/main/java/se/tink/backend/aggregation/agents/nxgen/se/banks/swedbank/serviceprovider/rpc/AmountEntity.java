package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currencyCode;
    private String amount;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public Amount toTinkAmount(String defaultCurrency) {
        if (Strings.isNullOrEmpty(currencyCode)) {
            return new Amount(defaultCurrency, AgentParsingUtils.parseAmount(amount));
        }

        return new Amount(currencyCode, AgentParsingUtils.parseAmount(amount));
    }

    public ExactCurrencyAmount toTinkAmount() {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return null;
        }

        return ExactCurrencyAmount.of(AgentParsingUtils.parseAmount(amount), currencyCode);
    }
}
