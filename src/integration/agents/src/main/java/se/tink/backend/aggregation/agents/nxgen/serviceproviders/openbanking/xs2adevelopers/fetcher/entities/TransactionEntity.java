package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private AmountEntity transactionAmount;
    private String debtorName;
    private String bankTransactionCode;
    private String transactionId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    private Transaction toTinkTransaction(boolean pending) {
        TransactionDates transactionDates =
                TransactionDates.builder()
                        .setValueDate(new AvailableDateInformation(valueDate))
                        .build();

        return Transaction.builder()
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                        getTransactionId())
                .setTransactionDates(transactionDates)
                .setTransactionReference(bankTransactionCode)
                .setDate(bookingDate)
                .setPending(pending)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toTinkAmount())
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL,
                        getCounterPartyName())
                .build();
    }

    private String getTransactionId() {
        return Optional.ofNullable(transactionId).orElse("");
    }

    private String getDescription() {
        if (!isExpense()) {
            if (StringUtils.isNotEmpty(debtorName)) {
                return debtorName;
            }
        } else if (StringUtils.isNotEmpty(creditorName)) {
            if ((creditorName.toLowerCase().contains("paypal")
                            || creditorName.toLowerCase().contains("klarna"))
                    && StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
                return remittanceInformationUnstructured;
            } else {
                return creditorName;
            }
        }
        return Optional.ofNullable(remittanceInformationUnstructured)
                .orElse(remittanceInformationStructured);
    }

    private boolean isExpense() {
        return transactionAmount.toTinkAmount().getExactValue().compareTo(BigDecimal.ZERO) < 0;
    }

    private String getCounterPartyName() {
        return Stream.of(creditorName, debtorName).filter(Objects::nonNull).findFirst().orElse("");
    }
}
