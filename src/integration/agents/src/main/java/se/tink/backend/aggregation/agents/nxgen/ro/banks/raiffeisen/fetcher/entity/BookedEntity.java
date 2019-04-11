package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BookedEntity {
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

    private boolean isNegative() {

        if (!Strings.isNullOrEmpty(debtorName)
                || (debtorAccount != null
                        && (!Strings.isNullOrEmpty(debtorAccount.getAccountNumber())
                                || !Strings.isNullOrEmpty(debtorAccount.getIban())))) {
            return false;
        }

        return true;
    }

    private Amount toTinkAmount() {
        return new Amount(
                transactionAmount.getCurrency(), transactionAmount.getAmount(isNegative()));
    }

    private Date toTinkDate() {
        try {
            if (!Strings.isNullOrEmpty(bookingDate)) {
                return new SimpleDateFormat(RaiffeisenConstants.DATE.FORMAT).parse(bookingDate);
            }
            return new SimpleDateFormat(RaiffeisenConstants.DATE.FORMAT).parse(valueDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(); // TODO: fix
    }

    private String getDescription() {

        /**
         * Prio: 1. We have creditorName and unstructured message 1.5. We have creditorName 2. We
         * have debtorName and unstructured message 2.5. We have debtorName 3. We have merchant name
         * though structure info 4. We have unstructured info 5. We have something else in
         * structured info 6. We have nothing => "Missing Description"
         */
        if (!Strings.isNullOrEmpty(creditorName)) {
            if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
                return creditorName + ": " + remittanceInformationUnstructured;
            }
            return creditorName;
        }

        if (!Strings.isNullOrEmpty(debtorName)) {
            if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
                return debtorName + ": " + remittanceInformationUnstructured;
            }
            return debtorName;
        }

        if (!Strings.isNullOrEmpty(remittanceInformationStructured)) {
            Matcher matcher =
                    RaiffeisenConstants.REGEX.PATTERN_STRUCTURED_INFO.matcher(
                            remittanceInformationStructured);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        }

        if (!Strings.isNullOrEmpty(remittanceInformationStructured)) {
            return remittanceInformationStructured;
        }

        return "<Missing Description>";
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(toTinkDate())
                .setAmount(toTinkAmount())
                .setPending(false)
                .build();
    }
}
