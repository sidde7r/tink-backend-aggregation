package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalanceEntity {
    private String accountId;

    private AmountEntity amount;

    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    private UkObBalanceType type;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant dateTime;

    private List<CreditLineEntity> creditLine;

    public ExactCurrencyAmount getAmount() {
        ExactCurrencyAmount unsignedAmount =
                ExactCurrencyAmount.of(amount.getUnsignedAmount(), amount.getCurrency());

        return UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT.equals(creditDebitIndicator)
                ? unsignedAmount
                : unsignedAmount.negate();
    }
}
