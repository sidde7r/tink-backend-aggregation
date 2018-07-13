package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Maps;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.LoanDetails;
import se.tink.libraries.date.ThreadSafeDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @JsonProperty("InterestRatesDetails")
    private List<Map<String, String>> interestRatesDetails;

    @JsonProperty("LoanDetails")
    private List<Map<String,String>> loanDetails;

    private Map<String, String> transformedLoanDetails;

    @JsonProperty("LoanNumber")
    private String loanNumber;

    @JsonProperty("PresentDebt")
    private String presentDebt;

    @JsonProperty("Type")
    private String type;

    public String getLoanNumber() {
        return this.loanNumber;
    }

    public String getType() {
        return this.type;
    }

    public String getPresentDebt() {
        return this.presentDebt;
    }

    public Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(transformedLoanDetails.get("Aktuell räntesats"));
    }

    public String getLoanName() {
        return transformedLoanDetails.get("Lån");
    }

    public Double getInitialDebt() {
        String initialDebt = transformedLoanDetails.get("Ursprunglig skuld");
        if (!Strings.isNullOrEmpty(initialDebt)) {
            return AgentParsingUtils.parseAmountTrimCurrency(initialDebt);
        }
        return null;
    }

    public Date getInitialDate () throws ParseException {
        String initialDate = transformedLoanDetails.get("Utbetalningsdag");
        if (!Strings.isNullOrEmpty(initialDate)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(initialDate);
        }
        return null;
    }

    public String getApplicants() {
        return transformedLoanDetails.get("Låntagare");
    }

    public List<Map<String, String>> getLoanDetails() {
        return this.loanDetails;
    }

    public List<Map<String, String>> getInterestRatesDetails() {
        return this.interestRatesDetails;
    }

    public void setInterestRatesDetails(List<Map<String, String>> interestRatesDetails) {
        this.interestRatesDetails = interestRatesDetails;
    }

    public void setLoanDetails(List<Map<String, String>> loanDetails) {
        this.loanDetails = loanDetails;

        Map<String,String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : loanDetails) {
            map.put(keyValuePair.get("Key"), keyValuePair.get("Value"));
        }

        this.transformedLoanDetails = map;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public void setPresentDebt(String presentDebt) {
        this.presentDebt = presentDebt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(getLoanNumber());
        account.setAccountNumber(getLoanNumber());
        account.setName(getType());
        if (!Strings.isNullOrEmpty(presentDebt)) {
            account.setBalance(-AgentParsingUtils.parseAmountTrimCurrency(presentDebt));
        }
        account.setType(AccountTypes.LOAN);

        return account;
    }

    public Loan toLoan() throws ParseException, JsonProcessingException {
        Loan loan = new Loan();
        LoanDetails loanDetails = new LoanDetails();

        loan.setType(Loan.Type.BLANCO);
        loan.setInterest(getInterestRate());
        loan.setName(getLoanName());
        loan.setInitialDate(getInitialDate());
        loan.setLoanNumber(getLoanNumber());

        if (getInitialDebt() != null ) {
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

        loan.setLoanDetails(loanDetails);
        loan.setSerializedLoanResponse(MAPPER.writeValueAsString(this));

        return loan;
    }
}
