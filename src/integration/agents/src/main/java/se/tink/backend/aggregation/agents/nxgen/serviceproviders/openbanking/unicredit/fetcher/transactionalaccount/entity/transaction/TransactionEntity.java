package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Getter
@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String entryReference;
    private String endToEndId;

    @JsonFormat(pattern = UnicreditConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = UnicreditConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date valueDate;

    private AmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountInfoEntity creditorAccount;
    private String debtorName;
    private TransactionAccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private String creditorId;
    private String mandateId;

    public Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationUnstructured)
                .setPending(isPending)
                .build();
    }
}
