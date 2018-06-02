package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

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

    public Amount toTinkAmount() {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return null;
        }

        return new Amount(currencyCode, AgentParsingUtils.parseAmount(amount));
    }
}
