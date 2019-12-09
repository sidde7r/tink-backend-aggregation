package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    public String transactionId;
    public String endToEndId;

    @JsonFormat(pattern = UnicreditConstants.Formats.DEFAULT_DATE_FORMAT)
    public Date bookingDate;

    @JsonFormat(pattern = UnicreditConstants.Formats.DEFAULT_DATE_FORMAT)
    public Date valueDate;

    public AmountEntity transactionAmount;
    public String creditorName;
    public TransactionAccountInfoEntity creditorAccount;
    public String debtorName;
    public TransactionAccountInfoEntity debtorAccount;
    public String remittanceInformationUnstructured;
    public String creditorId;
    public String mandateId;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationUnstructured)
                .setPending(false)
                .build();
    }
}
