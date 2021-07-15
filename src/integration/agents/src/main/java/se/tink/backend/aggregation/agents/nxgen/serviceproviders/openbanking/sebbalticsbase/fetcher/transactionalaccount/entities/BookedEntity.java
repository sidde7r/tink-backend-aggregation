package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.AccountNumberEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class BookedEntity {
    private String transactionId;
    private TransactionAmountEntity transactionAmount;
    private String type;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String entryReference;
    private String transactionPaymentInfId;
    private AccountNumberEntity creditorAccount;
    private String creditorName;
    private String endToEndIdentification;
    private String instructedIdentification;
    private CreditorIdEntity creditorId;
    private String creditorAddress;
    private String ultimateCreditor;
    private CreditorIdEntity ultimateCreditorId;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private String id;

    public Transaction toTinkTransaction(SebBalticsBaseApiClient apiClient) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.getAmount())
                        .setDate(bookingDate)
                        .setDescription(remittanceInformationUnstructured)
                        .setPending(false)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(purposeCode)
                        .setProviderMarket(apiClient.getProviderMarketCode());

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(valueDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
