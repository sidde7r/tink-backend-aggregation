package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.LoanDetails;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossKeyLoanDetails {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String kriterier;
    private String debitAccount1;
    private String debitAccount2;
    private String whitdrawWhere;
    private String paydate;
    private Double overdueInterest;
    private String intervall;
    private String period;
    private Double interestAmountFallenDue;
    private String paydate2;
    private String currency;
    private String insuredPerson2;
    private String otherOwnerName;
    private String ownerName;
    private String openingdate;
    private String meddelandeKod;
    private String showavi;
    private Double interestAmount;
    private String nextInterestAdjustmentDate;
    private Double fee;
    private Double feesFallenDue;
    private String reference;
    private String loanStatus;
    private String showTotalAmount;
    private String debitBbanAccount2;
    private Double instalment;
    private int respiteMonthsLeft;
    private String accountText;
    private Double instalmentFallenDue;
    private String tointerestperiod;
    private String nickName;
    private String accountTypeCode;
    private String liftable;
    private String debitBbanAccount1;
    private String estimEndDate;
    private String accountNumber;
    private Double insuranceAmountFallenDue;
    private Double grantedAmount;
    private String showSecondInterestAmount;
    private String fromiterestperiod;
    private String accountTypeName;
    private String iban;
    private String showIP;
    private String loanInsurance;
    private Double totalamount;
    private Double interestCeiling;
    private String insurancePremium;
    private Double interestToPay;
    private String insuredPerson1;
    private String interestBinding;
    private Double currentlyWithdrawableAmount;
    private String enddate;
    private Boolean interestCeilingShow;
    private Double availableAmount;
    private Boolean showNextPaymentInfo;
    private String acountTextShow;
    private String accountTypeNumber;
    private Double unUsedAmountToday;
    private Double interestRate;
    private Double insuranceAmount;
    private String indebtednessType;
    private String show1;
    private String show2;
    private Double withdrawnAmount;

    public Loan toTinkLoan() throws JsonProcessingException, ParseException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setName(Optional.ofNullable(getNickName()).orElse(getAccountTypeName()));
        loan.setLoanNumber(getAccountNumber());
        loan.setInitialDate(getOpeningdate());
        loan.setInitialBalance(getGrantedAmount());
        loan.setBalance(getAvailableAmount());
        loan.setInterest(getInterestRate());
        loan.setNextDayOfTermsChange(getNextInterestAdjustmentDate());

        loanDetails.setCoApplicant(hasCoApplicant());
        loanDetails.setApplicants(getApplicants());
        loan.setLoanDetails(loanDetails);

        String serializedResponse = MAPPER.writeValueAsString(this);
        loan.setSerializedLoanResponse(serializedResponse);

        return loan;
    }

    private Boolean hasCoApplicant() {
        return Strings.isNullOrEmpty(otherOwnerName);
    }

    private List<String> getApplicants() {
        List<String> applicants = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(ownerName)) {
            applicants.add(ownerName);

            if (!Strings.isNullOrEmpty(otherOwnerName)) {
                applicants.add(otherOwnerName);
            }
        }
        return applicants;
    }

    public String getKriterier() {
        return kriterier;
    }

    public void setKriterier(String kriterier) {
        this.kriterier = kriterier;
    }

    public String getDebitAccount1() {
        return debitAccount1;
    }

    public void setDebitAccount1(String debitAccount1) {
        this.debitAccount1 = debitAccount1;
    }

    public String getDebitAccount2() {
        return debitAccount2;
    }

    public void setDebitAccount2(String debitAccount2) {
        this.debitAccount2 = debitAccount2;
    }

    public String getWhitdrawWhere() {
        return whitdrawWhere;
    }

    public void setWhitdrawWhere(String whitdrawWhere) {
        this.whitdrawWhere = whitdrawWhere;
    }

    public String getPaydate() {
        return paydate;
    }

    public void setPaydate(String paydate) {
        this.paydate = paydate;
    }

    public Double getOverdueInterest() {
        return overdueInterest;
    }

    public void setOverdueInterest(Double overdueInterest) {
        this.overdueInterest = overdueInterest;
    }

    public String getIntervall() {
        return intervall;
    }

    public void setIntervall(String intervall) {
        this.intervall = intervall;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getInterestAmountFallenDue() {
        return interestAmountFallenDue;
    }

    public void setInterestAmountFallenDue(Double interestAmountFallenDue) {
        this.interestAmountFallenDue = interestAmountFallenDue;
    }

    public String getPaydate2() {
        return paydate2;
    }

    public void setPaydate2(String paydate2) {
        this.paydate2 = paydate2;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getInsuredPerson2() {
        return insuredPerson2;
    }

    public void setInsuredPerson2(String insuredPerson2) {
        this.insuredPerson2 = insuredPerson2;
    }

    public String getOtherOwnerName() {
        return otherOwnerName;
    }

    public void setOtherOwnerName(String otherOwnerName) {
        this.otherOwnerName = otherOwnerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Date getOpeningdate() throws ParseException {
        if (!Strings.isNullOrEmpty(openingdate) && !openingdate.equals("00000000")) {
            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(openingdate);
        }
        return null;
    }

    public void setOpeningdate(String openingdate) {
        this.openingdate = openingdate;
    }

    public String getMeddelandeKod() {
        return meddelandeKod;
    }

    public void setMeddelandeKod(String meddelandeKod) {
        this.meddelandeKod = meddelandeKod;
    }

    public String getShowavi() {
        return showavi;
    }

    public void setShowavi(String showavi) {
        this.showavi = showavi;
    }

    public Double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(Double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public Date getNextInterestAdjustmentDate() throws ParseException {
        if (!Strings.isNullOrEmpty(nextInterestAdjustmentDate) && !nextInterestAdjustmentDate.equals("00000000")) {
            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(nextInterestAdjustmentDate);
        }
        return null;
    }

    public void setNextInterestAdjustmentDate(String nextInterestAdjustmentDate) {
        this.nextInterestAdjustmentDate = nextInterestAdjustmentDate;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getFeesFallenDue() {
        return feesFallenDue;
    }

    public void setFeesFallenDue(Double feesFallenDue) {
        this.feesFallenDue = feesFallenDue;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(String loanStatus) {
        this.loanStatus = loanStatus;
    }

    public String getShowTotalAmount() {
        return showTotalAmount;
    }

    public void setShowTotalAmount(String showTotalAmount) {
        this.showTotalAmount = showTotalAmount;
    }

    public String getDebitBbanAccount2() {
        return debitBbanAccount2;
    }

    public void setDebitBbanAccount2(String debitBbanAccount2) {
        this.debitBbanAccount2 = debitBbanAccount2;
    }

    public Double getInstalment() {
        return instalment;
    }

    public void setInstalment(Double instalment) {
        this.instalment = instalment;
    }

    public int getRespiteMonthsLeft() {
        return respiteMonthsLeft;
    }

    public void setRespiteMonthsLeft(int respiteMonthsLeft) {
        this.respiteMonthsLeft = respiteMonthsLeft;
    }

    public String getAccountText() {
        return accountText;
    }

    public void setAccountText(String accountText) {
        this.accountText = accountText;
    }

    public Double getInstalmentFallenDue() {
        return instalmentFallenDue;
    }

    public void setInstalmentFallenDue(Double instalmentFallenDue) {
        this.instalmentFallenDue = instalmentFallenDue;
    }

    public String getTointerestperiod() {
        return tointerestperiod;
    }

    public void setTointerestperiod(String tointerestperiod) {
        this.tointerestperiod = tointerestperiod;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getLiftable() {
        return liftable;
    }

    public void setLiftable(String liftable) {
        this.liftable = liftable;
    }

    public String getDebitBbanAccount1() {
        return debitBbanAccount1;
    }

    public void setDebitBbanAccount1(String debitBbanAccount1) {
        this.debitBbanAccount1 = debitBbanAccount1;
    }

    public String getEstimEndDate() {
        return estimEndDate;
    }

    public void setEstimEndDate(String estimEndDate) {
        this.estimEndDate = estimEndDate;
    }

    public String getAccountNumber() {
        if (!Strings.isNullOrEmpty(accountNumber)) {
            // accountNumber contains spaces, lets remove them in order be somewhat future proof
            return accountNumber.replaceAll("\\s", "");
        }
        return null;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getInsuranceAmountFallenDue() {
        return insuranceAmountFallenDue;
    }

    public void setInsuranceAmountFallenDue(Double insuranceAmountFallenDue) {
        this.insuranceAmountFallenDue = insuranceAmountFallenDue;
    }

    public Double getGrantedAmount() {
        // is a negative number
        return grantedAmount;
    }

    public void setGrantedAmount(Double grantedAmount) {
        this.grantedAmount = grantedAmount;
    }

    public String getShowSecondInterestAmount() {
        return showSecondInterestAmount;
    }

    public void setShowSecondInterestAmount(String showSecondInterestAmount) {
        this.showSecondInterestAmount = showSecondInterestAmount;
    }

    public String getFromiterestperiod() {
        return fromiterestperiod;
    }

    public void setFromiterestperiod(String fromiterestperiod) {
        this.fromiterestperiod = fromiterestperiod;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getShowIP() {
        return showIP;
    }

    public void setShowIP(String showIP) {
        this.showIP = showIP;
    }

    public String getLoanInsurance() {
        return loanInsurance;
    }

    public void setLoanInsurance(String loanInsurance) {
        this.loanInsurance = loanInsurance;
    }

    public Double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(Double totalamount) {
        this.totalamount = totalamount;
    }

    public Double getInterestCeiling() {
        return interestCeiling;
    }

    public void setInterestCeiling(Double interestCeiling) {
        this.interestCeiling = interestCeiling;
    }

    public String getInsurancePremium() {
        return insurancePremium;
    }

    public void setInsurancePremium(String insurancePremium) {
        this.insurancePremium = insurancePremium;
    }

    public Double getInterestToPay() {
        return interestToPay;
    }

    public void setInterestToPay(Double interestToPay) {
        this.interestToPay = interestToPay;
    }

    public String getInsuredPerson1() {
        return insuredPerson1;
    }

    public void setInsuredPerson1(String insuredPerson1) {
        this.insuredPerson1 = insuredPerson1;
    }

    public String getInterestBinding() {
        return interestBinding;
    }

    public void setInterestBinding(String interestBinding) {
        this.interestBinding = interestBinding;
    }

    public Double getCurrentlyWithdrawableAmount() {
        return currentlyWithdrawableAmount;
    }

    public void setCurrentlyWithdrawableAmount(Double currentlyWithdrawableAmount) {
        this.currentlyWithdrawableAmount = currentlyWithdrawableAmount;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public Boolean getInterestCeilingShow() {
        return interestCeilingShow;
    }

    public void setInterestCeilingShow(Boolean interestCeilingShow) {
        this.interestCeilingShow = interestCeilingShow;
    }

    public Double getAvailableAmount() {
        // is a negative value
        return availableAmount;
    }

    public void setAvailableAmount(Double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public Boolean getShowNextPaymentInfo() {
        return showNextPaymentInfo;
    }

    public void setShowNextPaymentInfo(Boolean showNextPaymentInfo) {
        this.showNextPaymentInfo = showNextPaymentInfo;
    }

    public String getAcountTextShow() {
        return acountTextShow;
    }

    public void setAcountTextShow(String acountTextShow) {
        this.acountTextShow = acountTextShow;
    }

    public String getAccountTypeNumber() {
        return accountTypeNumber;
    }

    public void setAccountTypeNumber(String accountTypeNumber) {
        this.accountTypeNumber = accountTypeNumber;
    }

    public Double getUnUsedAmountToday() {
        return unUsedAmountToday;
    }

    public void setUnUsedAmountToday(Double unUsedAmountToday) {
        this.unUsedAmountToday = unUsedAmountToday;
    }

    public Double getInterestRate() {
        // Example of `interestRate`: 1.35
        BigDecimal interest = new BigDecimal(interestRate);
        interest = interest.divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP);
        return interest.doubleValue();
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getInsuranceAmount() {
        return insuranceAmount;
    }

    public void setInsuranceAmount(Double insuranceAmount) {
        this.insuranceAmount = insuranceAmount;
    }

    public String getIndebtednessType() {
        return indebtednessType;
    }

    public void setIndebtednessType(String indebtednessType) {
        this.indebtednessType = indebtednessType;
    }

    public String getShow1() {
        return show1;
    }

    public void setShow1(String show1) {
        this.show1 = show1;
    }

    public String getShow2() {
        return show2;
    }

    public void setShow2(String show2) {
        this.show2 = show2;
    }

    public Double getWithdrawnAmount() {
        return withdrawnAmount;
    }

    public void setWithdrawnAmount(Double withdrawnAmount) {
        this.withdrawnAmount = withdrawnAmount;
    }
}
