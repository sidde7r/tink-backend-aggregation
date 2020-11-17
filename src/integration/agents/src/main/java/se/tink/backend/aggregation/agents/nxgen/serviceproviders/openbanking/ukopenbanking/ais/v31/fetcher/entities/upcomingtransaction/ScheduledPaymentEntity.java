package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.upcomingtransaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ScheduledPaymentEntity {
    private String accountId;

    private String scheduledPaymentId;

    private ZonedDateTime scheduledPaymentDateTime;

    private String scheduledType;

    private AmountEntity instructedAmount;

    private CreditorAccountEntity creditorAccount;

    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                instructedAmount.getUnsignedAmount(),
                                instructedAmount.getCurrency()))
                .setDateTime(scheduledPaymentDateTime)
                .setDescription(null) // No description available
                .build();
    }

    @JsonProperty("ScheduledPaymentDateTime")
    private void setScheduledPaymentDateTime(String date) {
        if (!Strings.isNullOrEmpty(date)) {
            scheduledPaymentDateTime = ZonedDateTime.parse(date);
        }
    }
}
