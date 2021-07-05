package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public abstract class TransactionDetailsBaseEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date bookingDate;

    protected CreditorAccountEntity creditorAccount;
    protected String creditorId;
    protected String creditorName;
    protected DebtorAccountEntity debtorAccount;
    protected String debtorName;
    protected String mandateId;
    protected String remittanceInformationUnstructured;
    protected BalanceAmountBaseEntity transactionAmount;
    protected String transactionId;
    protected String entryReference;

    public String getEntryReference() {
        return entryReference;
    }

    public String getTransactionDescription() {
        if (Strings.isNullOrEmpty(creditorName) && Strings.isNullOrEmpty(debtorName)) {
            return remittanceInformationUnstructured;
        }
        if (Strings.isNullOrEmpty(debtorName)) {
            return creditorName;
        }
        return debtorName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date valueDate;

    public abstract Transaction toTinkTransaction();
}
