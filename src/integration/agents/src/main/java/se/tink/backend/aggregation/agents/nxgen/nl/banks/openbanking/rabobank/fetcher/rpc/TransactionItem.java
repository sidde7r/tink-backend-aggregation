package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionItem {

    @JsonProperty("debtorAccount")
    private DebtorAccount debtorAccount;

    @JsonProperty("creditorAgent")
    private String creditorAgent;

    @JsonProperty("creditorName")
    private String creditorName;

    @JsonProperty("debtorAgent")
    private String debtorAgent;

    @JsonProperty("debtorName")
    private String debtorName;

    @JsonProperty("remittanceInformationStructured")
    private String remittanceInformationStructured;

    @JsonProperty("ultimateCreditor")
    private String ultimateCreditor;

    @JsonProperty("raboBookingDateTime")
    private String raboBookingDateTime;

    @JsonProperty("valueDate")
    private String valueDate;

    @JsonProperty("endToEndId")
    private String endToEndId;

    @JsonProperty("ultimateDebtor")
    private String ultimateDebtor;

    @JsonProperty("exchangeRate")
    private List<ExchangeRateItem> exchangeRate;

    @JsonProperty("mandateId")
    private String mandateId;

    @JsonProperty("transactionAmount")
    private TransactionAmount transactionAmount;

    @JsonProperty("creditorAccount")
    private CreditorAccount creditorAccount;

    @JsonProperty("purposeCode")
    private String purposeCode;

    @JsonProperty("raboDetailedTransactionType")
    private String raboDetailedTransactionType;

    @JsonProperty("raboTransactionTypeName")
    private String raboTransactionTypeName;

    @JsonProperty("bookingDate")
    private String bookingDate;

    @JsonProperty("instructedAmount")
    private InstructedAmount instructedAmount;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured;

    @JsonProperty("initiatingPartyName")
    private String initiatingPartyName;

    @JsonProperty("creditorId")
    private String creditorId;

    public DebtorAccount getDebtorAccount() {
        return debtorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public String getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public ExactCurrencyAmount getTransactionAmount() {
        return transactionAmount.getAmount();
    }

    public CreditorAccount getCreditorAccount() {
        return creditorAccount;
    }

    public String getRaboDetailedTransactionType() {
        return raboDetailedTransactionType;
    }

    public String getRaboTransactionTypeName() {
        return raboTransactionTypeName;
    }

    public String getRemittanceInformationUnstructured() {
        return getFilteredRemittanceInformationUnstructured();
    }

    public String getInitiatingPartyName() {
        return initiatingPartyName;
    }

    @JsonIgnore
    public Date getBookedDate() {
        try {
            return new SimpleDateFormat(RabobankConstants.TRANSACTION_BOOKING_DATE_FORMAT)
                    .parse(bookingDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getFilteredRemittanceInformationUnstructured() {
        if (Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            return StringUtils.EMPTY;
        }
        return remittanceInformationUnstructured.replaceAll("\\s{2,}", " ").trim();
    }
}
