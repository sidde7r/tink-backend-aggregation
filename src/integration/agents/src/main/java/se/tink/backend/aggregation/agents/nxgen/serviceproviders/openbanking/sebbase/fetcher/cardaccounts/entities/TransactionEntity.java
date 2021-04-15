package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class TransactionEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String cardAcceptorCity;
    private String cardAcceptorCountryCode;
    private String cardTransactionId;
    private ExchangeRateEntity exchangeRate;
    private Boolean invoiced;
    private String maskedPan;
    private String nameOnCard;
    private OriginalAmountEntity originalAmount;
    private String proprietaryBankTransactionCode;
    private TransactionAmountEntity transactionAmount;
    private String transactionDetails;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public String getMaskedPan() {
        return Strings.emptyToNull(maskedPan);
    }
    public String getNameOnCard() {
        return Strings.emptyToNull(nameOnCard);
    }

    public CreditCardTransaction toTinkTransaction(boolean isPending) {
        return CreditCardTransaction.builder()
                .setAmount(transactionAmount.getTinkAmount())
                .setCreditCard(CreditCard.create(getNameOnCard(), getMaskedPan()))
                .setDate(valueDate)
                .setDescription(transactionDetails)
                .setPending(isPending)
                .build();
    }
}
