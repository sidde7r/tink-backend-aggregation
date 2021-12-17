package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
        if (isCreditTransaction()) {
            return creditorName;
        } else if (isDebitTransaction()) {
            return debtorName;
        }

        return remittanceInformationUnstructured;
    }

    public String getCounterpartyAccountIban() {
        return Optional.ofNullable(creditorAccount)
                .map(CreditorAccountEntity::getIban)
                .orElseGet(
                        () ->
                                Optional.ofNullable(debtorAccount)
                                        .map(DebtorAccountEntity::getIban)
                                        .orElse(null));
    }

    private boolean isDebitTransaction() {
        return StringUtils.isNotEmpty(debtorName);
    }

    private boolean isCreditTransaction() {
        return StringUtils.isNotEmpty(creditorName);
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date valueDate;

    public abstract Transaction toTinkTransaction();
}
