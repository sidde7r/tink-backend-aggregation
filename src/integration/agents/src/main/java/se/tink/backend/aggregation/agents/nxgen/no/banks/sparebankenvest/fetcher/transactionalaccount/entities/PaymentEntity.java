package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PaymentEntity {
    private String debitAccount;
    private String debitAccountGuid;
    private String creditAccount;
    private String paymentId;
    private String key;
    private String keyGuid;
    private String agreementId;
    private double amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date dueDate;

    private String note;
    private String cid;
    private String beneficiaryName;
    private String invoiceReference;
    private String providerId;
    private String brandId;
    private String statusEnum;
    private String statusDetailEnum;
    private String productEnum;
    private String paymentTypeEnum;
    private String receiptInfoEnum;
    private String messageCodeEnum;
    private boolean isPaymentStopped;
    private boolean isTransfer;

    @JsonIgnore
    public boolean isPaymentActive() {
        return !isPaymentInactive();
    }

    @JsonIgnore
    private boolean isPaymentInactive() {
        return isPaymentStopped
                && SparebankenVestConstants.Transactions.AGREEMENT_SUSPENDED.equalsIgnoreCase(
                        statusDetailEnum);
    }

    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inNOK(-amount))
                .setDate(dueDate)
                .setDescription(getDescription())
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (StringUtils.isNotBlank(beneficiaryName)) {
            return beneficiaryName;
        }

        return creditAccount;
    }
}
