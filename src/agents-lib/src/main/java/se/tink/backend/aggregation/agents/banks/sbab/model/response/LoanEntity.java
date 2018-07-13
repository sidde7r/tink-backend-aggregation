package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {

    private static final AggregationLogger log = new AggregationLogger(LoanEntity.class);
    private static final Pattern NUM_MONTHS_BOUND_PATTERN = Pattern.compile("(MONTHS_)([\\d]*)");

    @JsonProperty("bindningsbart")
    private boolean canBeConvertedToFixedRate;

    @JsonProperty("formedlarekontor")
    private SBABOfficeEntity sbabOffice;

    @JsonProperty("kunder")
    private List<LoanApplicantEntity> applicants;

    @JsonProperty("laneStatus")
    private int status;

    @JsonProperty("laneTyp")
    private int type;

    @JsonProperty("lanekapital")
    private double amount;

    @JsonProperty("lanenummer")
    private int loanNumber;

    @JsonProperty("laneobjekt")
    private LoanSecurityEntity security;

    @JsonProperty("lanevillkor")
    private LoanTermsEntity loanTerms;

    @JsonProperty("lantagareList")
    private List<LoanApplicantPartEntity> applicantParts;

    @JsonProperty("utbetalningsdag")
    private long initialPaymentDate;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(int loanNumber) {
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
     *  Note: this logic was taken from the JavaScript code at sbab.se. That is, the loan is a mortgage if the value
     *  laneobjekt.beteckning is present.
     */
    private boolean isMortgage() {
        return getSecurity() != null && !Strings.isNullOrEmpty(getSecurity().getLabel());
    }

    private Loan.Type getLoanType() {
        if (isMortgage()) {
            return Loan.Type.MORTGAGE;
        } else {
            return Loan.Type.OTHER;
        }
    }

    public Optional<Account> toTinkAccount() {
        Account account = new Account();

        if (Objects.equal(getLoanNumber(), 0)) {
            log.error("No loan number, can't create account");
            return Optional.empty();
        }

        String loanNumber = Integer.toString(getLoanNumber());
        account.setBankId(loanNumber);
        account.setAccountNumber(loanNumber);
        account.setName(loanNumber);
        account.setBalance(-getAmount());
        account.setType(AccountTypes.LOAN);

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

            if (Objects.equal(getLoanNumber(), 0)) {
                log.error("No loan number, can't create loan");
                return Optional.empty();
            }

            loan.setType(getLoanType());
            loan.setInterest(loanTerms.getNormalizedInterestRate());
            loan.setName(Integer.toString(getLoanNumber()));
            // If we would change this, also change the logic for when we fetch amortization documentation
            loan.setLoanNumber(Integer.toString(getLoanNumber()));
            loan.setBalance(-getAmount());

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
                Matcher matcher = NUM_MONTHS_BOUND_PATTERN.matcher(loanTerms.getInterestRateBoundPeriod());
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
}
