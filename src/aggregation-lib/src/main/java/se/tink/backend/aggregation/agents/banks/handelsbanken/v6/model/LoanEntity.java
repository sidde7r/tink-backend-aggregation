package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.LoanUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.LoanDetails;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {
    private List<LinkEntity> links;
    private List<Segment> segments;
    private String lenderId;
    private String lender;
    private String agreementNumber;
    private double currentDebt;
    private String currentDebtFormatted;
    private AmountEntity currentDebtAmount;
    private String interestRateFormatted;
    private String toPayFormatted;
    private boolean displayBadge;
    private boolean unpaid;
    private boolean displayDetails;
    private String unpaidText;
    private boolean contactOffice;
    private String contactOfficeText;
    private String fixationdateText;
    private static final ObjectMapper MAPPER = new ObjectMapper();


    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    public double getCurrentDebt() {
        return currentDebt;
    }

    public void setCurrentDebt(double currentDebt) {
        this.currentDebt = currentDebt;
    }

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public void setAgreementNumber(String agreementNumber) {
        this.agreementNumber = agreementNumber;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public Double getInterestRateFormatted() {
        if (!Strings.isNullOrEmpty(interestRateFormatted)) {
            return AgentParsingUtils.parsePercentageFormInterest(interestRateFormatted);
        }
        return null;
    }

    public void setInterestRateFormatted(String interestRateFormatted) {
        this.interestRateFormatted = interestRateFormatted;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(getAgreementNumber());
        account.setAccountNumber(getAgreementNumber());
        account.putIdentifier(new SwedishIdentifier(getAgreementNumber()));
        account.setName(getLender());
        account.setBalance(-getCurrentDebt());
        account.setType(AccountTypes.LOAN);

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches(
                        REGEXP_OR_JOINER.join("[0-9]{2}-[0-9]{6}-[0-9]{6}", "[0-9]{9}", "[0-9]{13}",
                                "[0-9]{4}( \\*{4}){2} [0-9]{4}")),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }

    public Loan toloan() throws JsonProcessingException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setInterest(getInterestRateFormatted());
        loan.setName(getLender());
        loan.setBalance(-getCurrentDebt());
        loan.setNextDayOfTermsChange(LoanUtils.parseNextDayOfTermsChange(this));
        loan.setNumMonthsBound(LoanUtils.parseNumMonthBound(this));
        loan.setLoanNumber(getAgreementNumber());
        loan.setMonthlyAmortization(LoanUtils.getAmortization(this));

        loanDetails.setCoApplicant(LoanUtils.hasCoApplicants(this));

        loan.setLoanDetails(loanDetails);
        loan.setSerializedLoanResponse(MAPPER.writeValueAsString(this));
        return loan;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public String getFixationdateText() {
        return fixationdateText != null ? fixationdateText.toLowerCase() : null;
    }

    public void setFixationdateText(String fixationdateText) {
        this.fixationdateText = fixationdateText;
    }

    public String getLenderId() {
        return lenderId;
    }

    public void setLenderId(String lenderId) {
        this.lenderId = lenderId;
    }

    public String getCurrentDebtFormatted() {
        return currentDebtFormatted;
    }

    public void setCurrentDebtFormatted(String currentDebtFormatted) {
        this.currentDebtFormatted = currentDebtFormatted;
    }

    public AmountEntity getCurrentDebtAmount() {
        return currentDebtAmount;
    }

    public void setCurrentDebtAmount(AmountEntity currentDebtAmount) {
        this.currentDebtAmount = currentDebtAmount;
    }

    public String getToPayFormatted() {
        return toPayFormatted;
    }

    public void setToPayFormatted(String toPayFormatted) {
        this.toPayFormatted = toPayFormatted;
    }

    public boolean isDisplayBadge() {
        return displayBadge;
    }

    public void setDisplayBadge(boolean displayBadge) {
        this.displayBadge = displayBadge;
    }

    public boolean isUnpaid() {
        return unpaid;
    }

    public void setUnpaid(boolean unpaid) {
        this.unpaid = unpaid;
    }

    public boolean isDisplayDetails() {
        return displayDetails;
    }

    public void setDisplayDetails(boolean displayDetails) {
        this.displayDetails = displayDetails;
    }

    public String getUnpaidText() {
        return unpaidText;
    }

    public void setUnpaidText(String unpaidText) {
        this.unpaidText = unpaidText;
    }

    public boolean isContactOffice() {
        return contactOffice;
    }

    public void setContactOffice(boolean contactOffice) {
        this.contactOffice = contactOffice;
    }

    public String getContactOfficeText() {
        return contactOfficeText;
    }

    public void setContactOfficeText(String contactOfficeText) {
        this.contactOfficeText = contactOfficeText;
    }
}
