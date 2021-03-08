package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.LoanDetails;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PCBW2581 {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private SecurityEntity securityEntity = new SecurityEntity();

    @JsonProperty("KONTRAKTNR")
    private String loanNumber;

    @JsonProperty("KTOSLAG_TXT")
    private String loanName;

    @JsonProperty("SKULD")
    private double currentDebt;

    @JsonProperty("RTE_SATS")
    private String interestRate;

    @JsonProperty("DATRTEJUST")
    private String nextDayOfTermsChange;

    @JsonProperty("AMORTERING")
    private Double monthlyAmortization;

    @JsonProperty("LANTAGARE1")
    private String applicant1;

    @JsonProperty("LANTAGARE2")
    private String applicant2;

    @JsonProperty("FLER_LANTAGARE_FL")
    private String multipleApplicants;

    @JsonProperty("OBJBETD1")
    private String objectInfo1;

    @JsonProperty("OBJBETD2")
    private String objectInfo2;

    @JsonProperty("OBJBETD3")
    private String objectInfo3;

    @JsonProperty("OBJBETD4")
    private String objectInfo4;
    /** The fields bellow are not used */
    @JsonProperty("ROW_ID")
    private int ROW_ID;

    @JsonProperty("NASTA_FFDAT")
    private String NASTA_FFDAT;

    @JsonProperty("BELOPP")
    private Double BELOPP;

    @JsonProperty("RTE_FF_DATUM")
    private String RTE_FF_DATUM;

    @JsonProperty("INBETSATT")
    private String INBETSATT;

    @JsonProperty("RANTA")
    private Double RANTA;

    @JsonProperty("DRJBEL")
    private Double DRJBEL;

    @JsonProperty("OVRAVGBEL")
    private Double OVRAVGBEL;

    @JsonProperty("KUNDNR_LOP_NR")
    private String KUNDNR_LOP_NR;

    @JsonProperty("LANTAGARE_FL")
    private String LANTAGARE_FL;

    @JsonProperty("DATSLUTBET")
    private String DATSLUTBET;

    @JsonProperty("AVISERING_FL")
    private String AVISERING_FL;

    @JsonProperty("FORFALL_KOD")
    private String FORFALL_KOD;

    @JsonProperty("AVI_TXT")
    private String AVI_TXT;

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(getLoanNumber());
        account.setAccountNumber(getLoanNumber());
        account.setName(getLoanName());
        account.setBalance(getCurrentDebt());
        account.setType(AccountTypes.LOAN);
        account.setCapabilities(SEBAgentUtils.getLoanAccountCapabilities());
        account.setSourceInfo(AccountSourceInfo.builder().bankProductName(getLoanName()).build());

        // Due to this agent being legacy we have to work with the rpc Account model directly. Using
        // the same logic as we do in core Account model when we map to the rpc Account.
        AccountHolder accountHolder = SEBAgentUtils.getTinkAccountHolder(applicant1, applicant2);
        account.setAccountHolder(accountHolder);
        account.setHolderName(
                SEBAgentUtils.getFirstHolder(accountHolder.getIdentities()).orElse(null));

        return account;
    }

    public Loan toLoan() throws JsonProcessingException, ParseException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setName(getLoanName());
        loan.setLoanNumber(getLoanNumber());
        loan.setBalance(getCurrentDebt());
        loan.setInterest(getInterestRate());
        loan.setMonthlyAmortization(getMonthlyAmortization());
        loan.setNextDayOfTermsChange(getNextDayOfTermsChange());
        loan.setNumMonthsBound(getNumMonthBoundFromName());
        // PCBW2581 only contains mortgages
        loan.setType(Loan.Type.MORTGAGE);

        loanDetails.setCoApplicant(hasCoApplicant());
        loanDetails.setApplicants(getApplicants());

        String security = MAPPER.writeValueAsString(securityEntity);
        loanDetails.setLoanSecurity(security);

        loan.setLoanDetails(loanDetails);

        String serializedResponse = MAPPER.writeValueAsString(this);
        loan.setSerializedLoanResponse(serializedResponse);

        return loan;
    }

    private Integer getNumMonthBoundFromName() {
        if (!Strings.isNullOrEmpty(loanName)) {
            Pattern pattern = Pattern.compile("BOLÅN - BOTTENLÅN MED (\\d?)-MÅNADERS RÄNTA");
            Matcher matcher = pattern.matcher(loanName.toUpperCase());
            if (matcher.find()) {
                return Integer.valueOf(matcher.group(1));
            }
        }
        return null;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public double getCurrentDebt() {
        return -currentDebt;
    }

    public void setCurrentDebt(double currentDebt) {
        this.currentDebt = currentDebt;
    }

    public Double getInterestRate() {
        if (!Strings.isNullOrEmpty(interestRate)) {
            return AgentParsingUtils.parsePercentageFormInterest(interestRate);
        }
        return null;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public Date getNextDayOfTermsChange() throws ParseException {
        if (!Strings.isNullOrEmpty(nextDayOfTermsChange)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(nextDayOfTermsChange);
        }
        return null;
    }

    public void setNextDayOfTermsChange(String nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }

    public Double getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public void setMonthlyAmortization(Double monthlyAmortization) {
        this.monthlyAmortization = monthlyAmortization;
    }

    public String getApplicant1() {
        return applicant1;
    }

    public void setApplicant1(String applicant1) {
        this.applicant1 = applicant1;
    }

    public String getApplicant2() {
        return applicant2;
    }

    public void setApplicant2(String applicant2) {
        this.applicant2 = applicant2;
    }

    public List<String> getApplicants() {
        List<String> applicants = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(applicant1)) {
            applicants.add(applicant1);

            if (!Strings.isNullOrEmpty(applicant2)) {
                applicants.add(applicant2);
            }
        }
        return applicants;
    }

    public String getObjectInfo1() {
        return objectInfo1;
    }

    public void setObjectInfo1(String objectInfo1) {
        this.objectInfo1 = objectInfo1;

        securityEntity.setObjectInfo1(objectInfo1);
    }

    public String getObjectInfo2() {
        return objectInfo2;
    }

    public void setObjectInfo2(String objectInfo2) {
        this.objectInfo2 = objectInfo2;

        securityEntity.setObjectInfo2(objectInfo2);
    }

    public String getObjectInfo3() {
        return objectInfo3;
    }

    public void setObjectInfo3(String objectInfo3) {
        this.objectInfo3 = objectInfo3;

        securityEntity.setObjectInfo3(objectInfo3);
    }

    public String getObjectInfo4() {
        return objectInfo4;
    }

    public void setObjectInfo4(String objectInfo4) {
        this.objectInfo4 = objectInfo4;

        securityEntity.setObjectInfo4(objectInfo4);
    }

    public String getMultipleApplicants() {
        return multipleApplicants;
    }

    public void setMultipleApplicants(String multipleApplicants) {
        this.multipleApplicants = multipleApplicants;
    }

    public Boolean hasCoApplicant() {
        if (Objects.equal(multipleApplicants, "J")) {
            return true;
        } else if (Objects.equal(multipleApplicants, "N")) {
            return false;
        }
        return null;
    }
}
