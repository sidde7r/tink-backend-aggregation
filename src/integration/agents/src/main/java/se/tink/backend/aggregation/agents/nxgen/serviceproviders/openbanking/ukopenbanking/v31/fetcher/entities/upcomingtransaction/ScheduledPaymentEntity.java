package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.upcomingtransaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class ScheduledPaymentEntity {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("ScheduledPaymentId")
    private String scheduledPaymentId;

    private ZonedDateTime scheduledPaymentDateTime;

    @JsonProperty("ScheduledType")
    private String scheduledType;

    @JsonProperty("InstructedAmount")
    private AmountEntity instructedAmount;

    @JsonProperty("CreditorAccount")
    private CreditorAccountEntity creditorAccount;

    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(instructedAmount)
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
