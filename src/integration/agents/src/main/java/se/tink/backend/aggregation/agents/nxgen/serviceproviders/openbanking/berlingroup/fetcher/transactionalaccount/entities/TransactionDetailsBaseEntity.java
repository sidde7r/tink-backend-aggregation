package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
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

    private boolean isDebitTransaction() {
        return Objects.nonNull(transactionAmount)
                && transactionAmount.toAmount().getExactValue().compareTo(BigDecimal.ZERO) < 0
                && StringUtils.isNotEmpty(debtorName);
    }

    private boolean isCreditTransaction() {
        return Objects.nonNull(transactionAmount)
                && transactionAmount.toAmount().getExactValue().compareTo(BigDecimal.ZERO) > 0
                && StringUtils.isNotEmpty(creditorName);
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date valueDate;

    public abstract Transaction toTinkTransaction();
}
