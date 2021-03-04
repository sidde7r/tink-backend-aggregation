package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
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

    public ExactCurrencyAmount toTinkAmount(String defaultCurrency) {
        if (Strings.isNullOrEmpty(currencyCode)) {
            ExactCurrencyAmount.of(AgentParsingUtils.parseAmount(amount), defaultCurrency);
        }

        return ExactCurrencyAmount.of(AgentParsingUtils.parseAmount(amount), currencyCode);
    }

    public ExactCurrencyAmount toTinkAmount() {
        return toTinkAmount("");
    }
}
