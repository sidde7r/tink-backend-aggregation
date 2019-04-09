package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Maps;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.LoanDetails;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageEntity {
    private static final Splitter SPLITTER = Splitter.on(CharMatcher.BREAKING_WHITESPACE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("Deeds")
    private String deeds;

    @JsonProperty("MortgageDetails")
    private List<Map<String, String>> mortgageDetails;

    private Map<String, String> transformedMortgageDetails;

    @JsonProperty("MortgageNumber")
    private String mortgageNumber;

    @JsonProperty("PresentDebt")
    private String presentDebt;

    public String getMortgageNumber() {
        return this.mortgageNumber;
    }

    public String getDeeds() {
        return this.deeds;
    }

    public String getPresentDebt() {
        return this.presentDebt;
    }

    public Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(
                transformedMortgageDetails.get("Aktuell räntesats"));
    }

    public Double getInitialDebt() {
        String initialDebt = transformedMortgageDetails.get("Ursprunglig skuld");
        if (!Strings.isNullOrEmpty(initialDebt)) {
            return AgentParsingUtils.parseAmountTrimCurrency(initialDebt);
        }
        return null;
    }

    public Date getNextDayOfTermsChange() throws ParseException {
        String nextDatOfTermsChange = transformedMortgageDetails.get("Nästa villkorsändringsdag");
        if (!Strings.isNullOrEmpty(nextDatOfTermsChange)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(nextDatOfTermsChange);
        }
        return null;
    }

    public Integer getNumMonthsBound() {
        String periodDescription = transformedMortgageDetails.get("Räntebindningstid");
        if (!Strings.isNullOrEmpty(periodDescription)) {
            String cleanPeriodDescription = periodDescription.replace("-", " ");

            Iterable<String> timeDescription = SPLITTER.split(cleanPeriodDescription);
            if (Iterables.size(timeDescription) == 3) {
                return AgentParsingUtils.parseNumMonthsBound(
                        Iterables.get(timeDescription, 1)
                                + " "
                                + Iterables.get(timeDescription, 2));
            }
        }
        return null;
    }

    public String getSecurity() {
        return transformedMortgageDetails.get("Säkerhet");
    }

    public String getTypeOfSecurity() {
        return transformedMortgageDetails.get("Typ av objekt");
    }

    public String getTypeOfLoan() {
        return transformedMortgageDetails.get("Typ av lån");
    }

    public String getApplicants() {
        return transformedMortgageDetails.get("Låntagare");
    }

    public List<Map<String, String>> getMortgageDetails() {
        return this.mortgageDetails;
    }

    public void setDeeds(String deeds) {
        this.deeds = deeds;
    }

    public void setMortgageNumber(String mortgageNumber) {
        this.mortgageNumber = mortgageNumber;
    }

    public void setPresentDebt(String presentDebt) {
        this.presentDebt = presentDebt;
    }

    public void setMortgageDetails(List<Map<String, String>> mortgageDetails) {
        this.mortgageDetails = mortgageDetails;

        Map<String, String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : mortgageDetails) {
            map.put(keyValuePair.get("Key"), keyValuePair.get("Value"));
        }

        this.transformedMortgageDetails = map;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(getMortgageNumber());
        account.setAccountNumber(getMortgageNumber());
        account.setName(getTypeOfLoan());
        if (getPresentDebt() != null) {
            account.setBalance(-AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt()));
        }
        account.setType(AccountTypes.LOAN);

        return account;
    }

    public Loan toLoan() throws ParseException, JsonProcessingException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setType(Loan.Type.MORTGAGE);
        loan.setInterest(getInterestRate());
        loan.setName(getTypeOfLoan());
        loan.setNextDayOfTermsChange(getNextDayOfTermsChange());
        loan.setLoanNumber(getMortgageNumber());
        loan.setNumMonthsBound(getNumMonthsBound());

        if (getInitialDebt() != null) {
            loan.setInitialBalance(-getInitialDebt());
        }

        if (getPresentDebt() != null) {
            loan.setBalance(-AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt()));
        }

        if (loan.getBalance() != null && loan.getInitialBalance() != null) {
            loan.setAmortized(loan.getBalance() - loan.getInitialBalance());
        }

        String applicants = getApplicants();
        ApplicantHelper applicantHelper = new ApplicantHelper(applicants);
        loanDetails.setApplicants(applicantHelper.getApplicants());
        loanDetails.setCoApplicant(applicantHelper.isCoApplicants());

        SecurityEntity securities = new SecurityEntity();
        securities.setSecurity(
                getSecurity()); // The getter and setter are from two different classes
        securities.setTypeOfSecurity(
                getTypeOfSecurity()); // The getter and setter are from two different classes
        loanDetails.setLoanSecurity(MAPPER.writeValueAsString(securities));

        loan.setLoanDetails(loanDetails);
        loan.setSerializedLoanResponse(MAPPER.writeValueAsString(this));

        return loan;
    }
}
