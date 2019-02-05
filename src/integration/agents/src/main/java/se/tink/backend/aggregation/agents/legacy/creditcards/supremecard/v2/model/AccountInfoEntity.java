package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoEntity {
    private String accountNumber;
    private String approvedCredit;
    private Date creationDate;
    private String negativeBalance;
    private String positiveBalance;
    private String reservedAmount;
    private String usableAmount;
    private Date createdDate;
    private String name;
    private String status;
    private String invoiceURL;
    private String paymentMissing;
    private String overdue;
    private List<CardEntity> cards;
    @JsonProperty("OCR")
    private OcrEntity ocr;
    private String createdYear;
    private String createdMonth;
    private List<String> monthNames;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getApprovedCredit() {
        return approvedCredit;
    }

    public void setApprovedCredit(String approvedCredit) {
        this.approvedCredit = approvedCredit;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getNegativeBalance() {
        return negativeBalance;
    }

    public void setNegativeBalance(String negativeBalance) {
        this.negativeBalance = negativeBalance;
    }

    public String getPositiveBalance() {
        return positiveBalance;
    }

    public void setPositiveBalance(String positiveBalance) {
        this.positiveBalance = positiveBalance;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public String getUsableAmount() {
        return usableAmount;
    }

    public void setUsableAmount(String usableAmount) {
        this.usableAmount = usableAmount;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInvoiceURL() {
        return invoiceURL;
    }

    public void setInvoiceURL(String invoiceURL) {
        this.invoiceURL = invoiceURL;
    }

    public String getPaymentMissing() {
        return paymentMissing;
    }

    public void setPaymentMissing(String paymentMissing) {
        this.paymentMissing = paymentMissing;
    }

    public String getOverdue() {
        return overdue;
    }

    public void setOverdue(String overdue) {
        this.overdue = overdue;
    }

    public List<CardEntity> getCards() {
        return cards;
    }

    public void setCards(List<CardEntity> cards) {
        this.cards = cards;
    }

    public OcrEntity getOcr() {
        return ocr;
    }

    public void setOcr(OcrEntity ocr) {
        this.ocr = ocr;
    }

    public String getCreatedYear() {
        return createdYear;
    }

    public void setCreatedYear(String createdYear) {
        this.createdYear = createdYear;
    }

    public String getCreatedMonth() {
        return createdMonth;
    }

    public void setCreatedMonth(String createdMonth) {
        this.createdMonth = createdMonth;
    }

    public List<String> getMonthNames() {
        return monthNames;
    }

    public void setMonthNames(List<String> monthNames) {
        this.monthNames = monthNames;
    }

    private double calculateBalance() {
        return -(AgentParsingUtils.parseAmount(positiveBalance) + AgentParsingUtils.parseAmount(reservedAmount));
    }

    public Account toAccount() {

        Account account = new Account();

        account.setBalance(calculateBalance());
        account.setAvailableCredit(AgentParsingUtils.parseAmount(approvedCredit));
        account.setName(StringUtils.formatHuman(name));
        account.setAccountNumber(accountNumber);
        account.setBankId(accountNumber);
        account.setType(AccountTypes.CREDIT_CARD);

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{16}"),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }
}
