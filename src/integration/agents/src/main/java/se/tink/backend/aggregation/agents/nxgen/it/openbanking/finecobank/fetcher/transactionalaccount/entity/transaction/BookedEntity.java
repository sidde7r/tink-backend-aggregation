package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {

    @JsonFormat(pattern = FinecoBankConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = FinecoBankConstants.Formats.DEFAULT_DATE_FORMAT)
    private Date valueDate;

    private String transactionId;
    private AmountEntity transactionAmount;
    private String creditorName;
    private TransactionAccountEntity creditorAccount;
    private String remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private String proprietaryBankTransactionCode;
    private LinksEntity links;
    private String debtorName;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(valueDate)
                .setAmount(transactionAmount.toTinkAmount())
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (!Strings.isNullOrEmpty(remittanceInformationStructured)) {
            return remittanceInformationStructured;
        } else if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        } else if (!Strings.isNullOrEmpty(proprietaryBankTransactionCode)) {
            return proprietaryBankTransactionCode;
        }
        return FormValues.MISSING_DESCRIPTION;
    }
}
