package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class PendingEntity {
    private String transactionId;
    private String endToEndId;
    private String mandateId;
    private String bookingDate;
    private String valueDate;
    private TransactionEntity transactionAmount;
    private List<ExchangeRateEntity> exchangeRate;
    private String creditorName;
    private AccountInfoEntity creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private AccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String purposeCode;
    private String bankTransactionCode;

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public String getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public String getBankTransactionCode() {
        return bankTransactionCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public TransactionEntity getTransactionAmount() {
        return transactionAmount;
    }

    public List<ExchangeRateEntity> getExchangeRate() {
        return exchangeRate;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public AccountInfoEntity getCreditorAccount() {
        return creditorAccount;
    }

    public String getUltimateCreditor() {
        return ultimateCreditor;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public AccountInfoEntity getDebtorAccount() {
        return debtorAccount;
    }

    private Amount toTinkAmount() {
        return new Amount(transactionAmount.getCurrency(), transactionAmount.getAmount());
    }

    private Date toTinkDate() {
        try {
            if(!Strings.isNullOrEmpty(bookingDate))
            {
                return new SimpleDateFormat(RaiffeisenConstants.DATE.FORMAT).parse(bookingDate);
            }
            return new SimpleDateFormat(RaiffeisenConstants.DATE.FORMAT).parse(valueDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(); // TODO: fix
    }

    private String getDescription() {
        if(!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        }
        return remittanceInformationStructured;
    }

    private HashMap<String, String> getPayload() {
        HashMap<String, String> result = new HashMap<>();

        result.put("currency", transactionAmount.getCurrency());
        result.put("provider", "ro-raiffeisen-psd2");

        return result;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(toTinkDate())
                .setAmount(toTinkAmount())
                .setRawDetails(getPayload())
                .setPending(true)
                .build();
    }
}
