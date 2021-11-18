package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.TransactionType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.AccountNumberEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@Slf4j
@JsonObject
public class BookedEntity {
    private String transactionId;
    private TransactionAmountEntity transactionAmount;
    private String type;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String entryReference;
    private String transactionPaymentInfId;
    private String debtorName;
    private AccountNumberEntity debtorAccount;
    private AccountNumberEntity creditorAccount;
    private String creditorName;
    private String endToEndIdentification;
    private String instructedIdentification;
    private IdEntity debtorId;
    private String debtorAddress;
    private String ultimateDebtor;
    private IdEntity ultimateDebtorId;
    private IdEntity creditorId;
    private String creditorAddress;
    private String ultimateCreditor;
    private IdEntity ultimateCreditorId;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private String id;

    public Transaction toTinkTransaction(String providerMarket) {
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
                        .setProviderMarket(providerMarket);

        if (TransactionType.CREDIT.equalsIgnoreCase(type)) {
            // According to SEB's documentation, type can only be "debit" or "credit".
            // The merchant name should be of the "opposite" type.
            // Eg. type = credit -> merchant name = debtorName.
            builder.setMerchantName(debtorName);
            if (debtorId != null) {
                builder.setMerchantCategoryCode(debtorId.getSchemeNameCode());
                builder.setProprietaryFinancialInstitutionType(debtorId.getType());
            }
        }
        if (TransactionType.DEBIT.equalsIgnoreCase(type)) {
            builder.setMerchantName(creditorName);
            if (creditorId != null) {
                builder.setMerchantCategoryCode(creditorId.getSchemeNameCode());
                builder.setProprietaryFinancialInstitutionType(creditorId.getType());
            }
        }

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(valueDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }
}
