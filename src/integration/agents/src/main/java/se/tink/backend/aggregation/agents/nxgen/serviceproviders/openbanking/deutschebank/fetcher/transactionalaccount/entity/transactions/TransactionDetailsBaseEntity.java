package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
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
    protected AmountEntity transactionAmount;
    protected String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date valueDate;

    public abstract Transaction toTinkTransaction();

    public String getDescription() {
        if (transactionAmount.toTinkAmount().getExactValue().compareTo(BigDecimal.ZERO) > 0) {
            if (StringUtils.isNotEmpty(debtorName)) {
                return debtorName;
            }
        } else if (StringUtils.isNotEmpty(creditorName)) {
            if ((creditorName.toLowerCase().contains("paypal")
                            || creditorName.toLowerCase().contains("klarna"))
                    && StringUtils.isNotEmpty(remittanceInformationUnstructured)) {
                return remittanceInformationUnstructured;
            } else {
                return creditorName;
            }
        }
        return remittanceInformationUnstructured;
    }
}
