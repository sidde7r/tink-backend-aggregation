package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.TransactionType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class PendingEntity {

    private LocalDate valueDate;
    private TransactionAmountEntity transactionAmount;
    private String pendingType;
    private String remittanceInformationUnstructured;
    private String creditorName;

    public Transaction toTinkTransaction(String providerMarket) {
        Builder builder =
                Transaction.builder()
                        .setAmount(getAmount())
                        .setDate(valueDate)
                        .setDescription(
                                isReserved() ? remittanceInformationUnstructured : creditorName)
                        .setPending(true)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(pendingType)
                        .setProviderMarket(providerMarket);

        return (Transaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(valueDate))
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        return isReserved()
                ? transactionAmount.getAmount().negate()
                : transactionAmount.getAmount();
    }

    public boolean isUpcoming() {
        return TransactionType.UPCOMING.equalsIgnoreCase(pendingType);
    }

    public boolean isReserved() {
        return TransactionType.RESERVED.equalsIgnoreCase(pendingType);
    }
}
