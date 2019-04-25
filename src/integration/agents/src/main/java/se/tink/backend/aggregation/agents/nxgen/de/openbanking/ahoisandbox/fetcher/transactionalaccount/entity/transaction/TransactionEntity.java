package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String type;
    private String id;
    private String transactionPatternId;
    private List<AdditionalInformationEntity> additionalInformation;
    private AmountEntity amount;
    private String creditor;
    private String creditorBankCode;
    private String creditorAccountNumber;
    private String debtor;
    private String debtorBankCode;
    private String debtorAccountNumber;
    private String purpose;
    private String cleanPurpose;
    private Boolean prebooked;
    private String bookingKey;
    private String mandateId;
    private String endToEndId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(valueDate)
                .setDescription(purpose)
                .setExternalId(id)
                .build();
    }
}
