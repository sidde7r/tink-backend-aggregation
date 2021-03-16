package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private CreditDebitIndicator creditDebitIndicator;
    private String entryReference;

    private RemittanceInformationEntity remittanceInformation;

    private String status;
    private TransactionAmount transactionAmount;
    private RelatedParties relatedParties;

    private AdditionalTransactionInformation additionalTransactionInformation;

    public Transaction toTinkModel() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setAmount(getAmount())
                .setDate(bookingDate)
                .setPending(status.equalsIgnoreCase(Transactions.PENDING_STATUS))
                .build();
    }

    private String getDescription() {
        return relatedParties != null ? getNameFromRelatedParties() : getOtherDescription();
    }

    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(
                new BigDecimal(transactionAmount.getAmount()), transactionAmount.getCurrency());
    }

    private String getNameFromRelatedParties() {
        return relatedParties.getDebtor() != null
                ? relatedParties.getDebtor().getName()
                : relatedParties.getCreditor().getName();
    }

    private String getOtherDescription() {
        return remittanceInformation != null
                ? getUnstructuredDesc()
                : getAdditionalInformationDesc();
    }

    private String getUnstructuredDesc() {
        return remittanceInformation.getUnstructured() != null
                ? remittanceInformation.getUnstructuredInformation()
                : getAdditionalInformationDesc();
    }

    private String getAdditionalInformationDesc() {
        return additionalTransactionInformation != null
                ? additionalTransactionInformation.getDescription()
                : "";
    }
}
