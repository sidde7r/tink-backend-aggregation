package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class PendingEntity {

    private LocalDate valueDate;

    private String descriptiveText;
    private String creditorName;
    private TransactionAmountEntity transactionAmount;
    private String pendingType;

    /** Documented but never set in response (checked 2021-04-01) */
    private String transactionId;

    /** Documented but never set in response (checked 2021-04-01) */
    private String creditorAccount;

    /** Documented but never set in response (checked 2021-04-01) */
    private String pendingTypeDetailed;

    /** Documented but never set in response (checked 2021-04-01) */
    private String remittanceInformationUnstructured;

    /** Documented but never set in response (checked 2021-04-01) */
    private long remittanceInformationStructuredReference;

    /** Documented but never set in response (checked 2021-04-01) */
    private String ownNotes;

    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(getAmount())
                        .setDate(valueDate)
                        .setDescription(isReserved() ? descriptiveText : creditorName)
                        .setPending(true)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProprietaryFinancialInstitutionType(pendingType)
                        .setProviderMarket(SebConstants.MARKET);

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
        return SebCommonConstants.TransactionType.UPCOMING.equalsIgnoreCase(pendingType);
    }

    public boolean isReserved() {
        return SebCommonConstants.TransactionType.RESERVED.equalsIgnoreCase(pendingType);
    }
}
