package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;

@Slf4j
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {

    private static final Pattern NUM_MONTHS_BOUND_PATTERN = Pattern.compile("(MONTHS_)([\\d]*)");

    @JsonProperty("bindningsbart")
    private boolean canBeConvertedToFixedRate;

    @JsonProperty("formedlarekontor")
    private SBABOfficeEntity sbabOffice;

    @JsonProperty("kunder")
    private List<LoanApplicantEntity> applicants;

    @JsonProperty("laneStatus")
    private long status;

    private String loanType;

    private BigDecimal loanAmount;
    private BigDecimal originalLoanAmount;
    private BigInteger loanNumber;

    @JsonProperty("laneobjekt")
    private LoanSecurityEntity security;

    private LoanTermsEntity loanTerms;

    @JsonProperty("lantagareList")
    private List<LoanApplicantPartEntity> applicantParts;

    @JsonProperty("utbetalningsdag")
    private long initialPaymentDate;

    public BigDecimal getAmount() {
        return loanAmount;
    }

    public void setAmount(BigDecimal amount) {
        this.loanAmount = amount;
    }

    public boolean isCanBeConvertedToFixedRate() {
        return canBeConvertedToFixedRate;
    }

    public void setCanBeConvertedToFixedRate(boolean canBeConvertedToFixedRate) {
        this.canBeConvertedToFixedRate = canBeConvertedToFixedRate;
    }

    public SBABOfficeEntity getSbabOffice() {
        return sbabOffice;
    }

    public void setSbabOffice(SBABOfficeEntity sbabOffice) {
        this.sbabOffice = sbabOffice;
    }

    public List<LoanApplicantEntity> getApplicants() {
        return applicants;
    }

    public void setApplicants(List<LoanApplicantEntity> applicants) {
        this.applicants = applicants;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String type) {
        this.loanType = type;
    }

    public BigInteger getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(BigInteger loanNumber) {
        this.loanNumber = loanNumber;
    }

    public LoanSecurityEntity getSecurity() {
        return security;
    }

    public void setSecurity(LoanSecurityEntity security) {
        this.security = security;
    }

    public List<LoanApplicantPartEntity> getApplicantParts() {
        return applicantParts;
    }

    public void setApplicantParts(List<LoanApplicantPartEntity> applicantParts) {
        this.applicantParts = applicantParts;
    }

    public long getInitialPaymentDate() {
        return initialPaymentDate;
    }

    public void setInitialPaymentDate(long initialPaymentDate) {
        this.initialPaymentDate = initialPaymentDate;
    }

    public LoanTermsEntity getLoanTerms() {
        return loanTerms;
    }

    public void setLoanTerms(LoanTermsEntity loanTerms) {
        this.loanTerms = loanTerms;
    }

    /**
     * Loan type mapping based on type number. We have seen that type 14, 60, 61, 62 are loans with
     * securities so they are mapped as mortgage. Type 70 are loans without securities and a higher
     * interest rate so they are mapped as blanco.
     */
    private Loan.Type getTypeOfLoan() {
        switch (loanType) {
            case "MORTGAGE_LOAN":
                return Loan.Type.MORTGAGE;
            case "BLANCO_MORTGAGE_LOAN":
                return Loan.Type.BLANCO;
            default:
                log.info("Unknown loan type {} categorised as other", loanType);
                return Loan.Type.OTHER;
        }
    }

    public Optional<Account> toTinkAccount() {
        Account account = new Account();

        if (Objects.equal(getLoanNumber().intValue(), 0)) {
            log.error("No loan number, can't create account");
            return Optional.empty();
        }

        String loanNumber = getLoanNumber().toString();
        account.setBankId(loanNumber);
        account.setAccountNumber(loanNumber);
        account.setName(loanNumber);
        account.setBalance(getAmount().negate().doubleValue());
        account.setType(AccountTypes.LOAN);
        account.setSourceInfo(createAccountSourceInfo());

        return Optional.of(account);
    }

    public Optional<Loan> toTinkLoan() {
        try {
            Loan loan = new Loan();
            LoanTermsEntity loanTerms = getLoanTerms();

            if (loanTerms == null) {
                log.error("No loan terms, can't create loan");
                return Optional.empty();
            }

            if (Objects.equal(getLoanNumber().intValue(), 0)) {
                log.error("No loan number, can't create loan");
                return Optional.empty();
            }

            loan.setType(getTypeOfLoan());
            loan.setInterest(loanTerms.getNormalizedInterestRate());
            loan.setName(getLoanNumber().toString());
            // If we would change this, also change the logic for when we fetch amortization
            // documentation
            loan.setLoanNumber(getLoanNumber().toString());
            loan.setBalance(getAmount().negate().doubleValue());

            if (!Objects.equal(loanTerms.getAmortizationValue(), 0)) {
                loan.setMonthlyAmortization(loanTerms.getAmortizationValue());
            }

            if (getInitialPaymentDate() != 0) {
                loan.setInitialDate(new Date(getInitialPaymentDate()));
            }

            if (loanTerms.getNextDayOfTermsChange() != 0) {
                loan.setNextDayOfTermsChange(new Date(loanTerms.getNextDayOfTermsChange()));
            }

            if (loanTerms.getInterestRateBoundPeriod() != null) {
                Matcher matcher =
                        NUM_MONTHS_BOUND_PATTERN.matcher(loanTerms.getInterestRateBoundPeriod());
                if (matcher.find()) {
                    int monthsBound = Integer.parseInt(matcher.group(2));
                    loan.setNumMonthsBound(monthsBound);
                }
            }

            return Optional.of(loan);

        } catch (Exception e) {
            log.error("Could not create loan", e);
            return Optional.empty();
        }
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankProductCode(String.valueOf(getTypeOfLoan())).build();
    }
}
