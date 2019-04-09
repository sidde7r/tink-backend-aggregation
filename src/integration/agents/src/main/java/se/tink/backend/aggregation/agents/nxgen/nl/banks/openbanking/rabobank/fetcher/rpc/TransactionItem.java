package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionItem {

    @JsonProperty("debtorAccount")
    private DebtorAccount debtorAccount;

    @JsonProperty("creditorAgent")
    private String creditorAgent;

    @JsonProperty("debtorAgent")
    private String debtorAgent;

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

    public void setDebtorAccount(DebtorAccount debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public String getDebtorAgent() {
        return debtorAgent;
    }

    public void setDebtorAgent(String debtorAgent) {
        this.debtorAgent = debtorAgent;
    }

    public String getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public String getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }

    public String getRaboBookingDateTime() {
        return raboBookingDateTime;
    }

    public void setRaboBookingDateTime(String raboBookingDateTime) {
        this.raboBookingDateTime = raboBookingDateTime;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    public List<ExchangeRateItem> getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(List<ExchangeRateItem> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public Amount getTransactionAmount() {
        return transactionAmount.getAmount();
    }

    public void setTransactionAmount(TransactionAmount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public CreditorAccount getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(CreditorAccount creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getRaboDetailedTransactionType() {
        return raboDetailedTransactionType;
    }

    public void setRaboDetailedTransactionType(String raboDetailedTransactionType) {
        this.raboDetailedTransactionType = raboDetailedTransactionType;
    }

    public String getRaboTransactionTypeName() {
        return raboTransactionTypeName;
    }

    public void setRaboTransactionTypeName(String raboTransactionTypeName) {
        this.raboTransactionTypeName = raboTransactionTypeName;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public InstructedAmount getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(InstructedAmount instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public String getInitiatingPartyName() {
        return initiatingPartyName;
    }

    public void setInitiatingPartyName(String initiatingPartyName) {
        this.initiatingPartyName = initiatingPartyName;
    }

    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
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
}
