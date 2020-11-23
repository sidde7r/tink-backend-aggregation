package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.LoanDetails;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDetailsEntity {
    private static final Logger log = LoggerFactory.getLogger(LoanDetailsEntity.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String loanName;
    private String loanNumber;
    private String originalDebt;
    private String currentDebt;
    private String currentInterestRate;
    private String rateBoundUntil;
    private String rateBindingPeriodLength;
    private List<BorrowerEntity> borrowers = Lists.newArrayList();
    private List<SecurityEntity> securities;
    private boolean fixedRate;

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    private Double getOriginalDebt() {
        if (!Strings.isNullOrEmpty(originalDebt)) {
            return -1 * Math.abs(AgentParsingUtils.parseAmount(originalDebt));
        }
        return null;
    }

    public void setOriginalDebt(String originalDebt) {
        this.originalDebt = originalDebt;
    }

    private Double getCurrentDebt() {
        if (!Strings.isNullOrEmpty(currentDebt)) {
            return -1 * Math.abs(AgentParsingUtils.parseAmount(currentDebt));
        }
        return null;
    }

    public void setCurrentDebt(String currentDebt) {
        this.currentDebt = currentDebt;
    }

    private Double getCurrentInterestRate() {
        if (!Strings.isNullOrEmpty(currentInterestRate)) {
            return AgentParsingUtils.parsePercentageFormInterest(currentInterestRate);
        }
        return null;
    }

    public void setCurrentInterestRate(String currentInterestRate) {
        this.currentInterestRate = currentInterestRate;
    }

    private Date getRateBoundUntil() throws ParseException {
        if (!Strings.isNullOrEmpty(rateBoundUntil)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(rateBoundUntil);
        }
        return null;
    }

    public void setRateBoundUntil(String rateBoundUntil) {
        this.rateBoundUntil = rateBoundUntil;
    }

    private int getRateBindingPeriodLength() {
        if (!Strings.isNullOrEmpty(rateBindingPeriodLength)) {
            return AgentParsingUtils.parseNumMonthsBound(rateBindingPeriodLength);
        }
        return 1;
    }

    public void setRateBindingPeriodLength(String rateBindingPeriodLength) {
        this.rateBindingPeriodLength = rateBindingPeriodLength;
    }

    private List<String> getBorrowers() {
        List<String> listOfBorrowers = Lists.newArrayList();
        for (BorrowerEntity borrower : borrowers) {
            listOfBorrowers.add(borrower.getName());
        }
        return listOfBorrowers;
    }

    public void setBorrowers(List<BorrowerEntity> borrowers) {
        this.borrowers = borrowers != null ? borrowers : Lists.<BorrowerEntity>newArrayList();
    }

    public List<SecurityEntity> getSecurities() {
        return securities;
    }

    public void setSecurities(List<SecurityEntity> securities) {
        this.securities = securities;
    }

    public boolean isFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(boolean fixedRate) {
        this.fixedRate = fixedRate;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(getLoanNumber());
        account.setAccountNumber(getLoanNumber());
        account.setName(getLoanName());
        account.setBalance(getCurrentDebt());
        account.setType(AccountTypes.LOAN);

        return account;
    }

    public Loan toLoan(String detailsString) throws ParseException, JsonProcessingException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setName(loanName);
        loan.setLoanNumber(loanNumber);
        loan.setNextDayOfTermsChange(getRateBoundUntil());
        loan.setInterest(getCurrentInterestRate());
        loan.setInitialBalance(getOriginalDebt());
        loan.setBalance(getCurrentDebt());
        loan.setNumMonthsBound(getRateBindingPeriodLength());
        loan.setType(getLoanType());
        loanDetails.setApplicants(getBorrowers());

        if (loan.getBalance() != null && loan.getInitialBalance() != null) {
            loan.setAmortized(loan.getBalance() - loan.getInitialBalance());
        }

        if (loanDetails.getApplicants() != null && loanDetails.getApplicants().size() > 1) {
            loanDetails.setCoApplicant(true);
        } else {
            loanDetails.setCoApplicant(false);
        }

        String loanSecurity = MAPPER.writeValueAsString(securities);
        loanDetails.setLoanSecurity(loanSecurity);

        loan.setLoanDetails(loanDetails);
        loan.setSerializedLoanResponse(detailsString);

        return loan;
    }

    private Loan.Type getLoanType() {
        // Really doubting that loan name will ever be null, but handling it so we don't get NPEs
        if (Strings.isNullOrEmpty(loanName)) {
            log.warn("No loan name present, can't determine loan type.");
            return Loan.Type.OTHER;
        }

        if (isMortgage()) {
            return Loan.Type.MORTGAGE;
        }

        if (isBlanco()) {
            return Loan.Type.BLANCO;
        }

        if (loanNameContains("bil")) {
            return Loan.Type.VEHICLE;
        }

        if (loanNameContains("lantbruk")) {
            return Loan.Type.LAND;
        }

        if (loanNameContains("företagslån")) {
            return Loan.Type.OTHER;
        }

        log.warn("Unknown loan type: {}", loanName);
        return Loan.Type.OTHER;
    }

    private boolean isMortgage() {
        return loanNameContains("bolån")
                || loanNameContains("borätt")
                || loanNameContains("villa")
                || loanNameContains("fastighetslån")
                || loanNameContains("topplån")
                || loanNameContains("byggnadslån")
                || loanNameContains("huslån");
    }

    private boolean isBlanco() {
        return loanNameContains("privatlån") || loanNameContains("blanco");
    }

    private boolean loanNameContains(String searchString) {
        return StringUtils.containsIgnoreCase(loanName, searchString);
    }
}
