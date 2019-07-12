package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardPendingEntity {

    private String cardTransactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private AmountEntity originalAmount;
    private String cardAcceptorCity;
    private String cardAcceptorCountryCode;
    private AmountEntity transactionAmount;
    private String proprietaryBankTransactionCode;
    private ExchangeRateEntity exchangeRate;
    private String transactionDetails;
    private String maskedPan;
    private boolean invoiced;
    private String nameOnCard;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(bookingDate)
                .setDescription(transactionDetails)
                .setPending(true)
                .build();
    }
}
