package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String transactionId;
    private String valueDate;
    private String debtorName;
    private AccountEntity debtorAccount;
    private String endToEndId;
    private String purposeCode;

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toAmount())
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(true)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toAmount())
                .build();
    }

    /**
     * Priority 1 - "purpose: creditorName: remittanceInformationUnstructured" Priority 2 -
     * "creditorName: remittanceInformationUnstructured" Priority 3 - "purpose: debtorName:
     * remittanceInformationUnstructured" Priority 4 - "debtorName:
     * remittanceInformationUnstructured" Priority 5 - "purpose: remittanceInformationUnstructured"
     * Priority 6 - "remittanceInformationUnstructured" Priority 7 - "purpose" Priority 8 -
     * "<Missing Description>"
     */
    private String getDescription() {

        if (!Strings.isNullOrEmpty(creditorName)) {
            if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
                if (!Strings.isNullOrEmpty(purposeCode)) {
                    return purposeCode
                            + ": "
                            + creditorName
                            + ": "
                            + remittanceInformationUnstructured;
                }
                return creditorName + ": " + remittanceInformationUnstructured;
            }
            return creditorName;
        }

        if (!Strings.isNullOrEmpty(debtorName)) {
            if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
                if (!Strings.isNullOrEmpty(purposeCode)) {
                    return purposeCode
                            + ": "
                            + debtorName
                            + ": "
                            + remittanceInformationUnstructured;
                }
                return debtorName + ": " + remittanceInformationUnstructured;
            }
            return debtorName;
        }

        if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            if (!Strings.isNullOrEmpty(purposeCode)) {
                return purposeCode + ": " + remittanceInformationUnstructured;
            }
            return remittanceInformationUnstructured;
        }

        if (!Strings.isNullOrEmpty(purposeCode)) {
            return purposeCode;
        }

        return "<Missing Description>";
    }
}
