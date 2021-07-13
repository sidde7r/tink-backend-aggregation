package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Slf4j
public class TransactionBaseEntity {

    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private AmountEntity transactionAmount;
    private String currencyExchange;
    private String creditorName;
    private AccountEntity creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private AccountEntity debtorAccount;
    private String ultimateDebtor;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String additionalInformation;
    private String purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;

    @JsonProperty("_links")
    private String links;

    public Transaction toTinkTransaction(String providerMarket, boolean isPending) {
        return (Transaction)
                Transaction.builder()
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDescription(remittanceInformationUnstructured)
                        .setPending(isPending)
                        .setDate(getTransactionDate())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(proprietaryBankTransactionCode)
                        .setProviderMarket(providerMarket)
                        .build();
    }

    private LocalDate getTransactionDate() {
        return Optional.ofNullable(bookingDate).orElse(valueDate);
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(valueDate));

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }
}
