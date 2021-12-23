package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalanceEntity {

    @JsonIgnore private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    public String prettyPrint() {
        return GSON.toJson(this);
    }
}
