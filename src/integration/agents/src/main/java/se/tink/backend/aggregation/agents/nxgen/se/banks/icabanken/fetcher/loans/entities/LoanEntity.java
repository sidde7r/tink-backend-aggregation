package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanParsingHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

@JsonObject
public class LoanEntity {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(LoanEntity.class);

    private String loanName;
    private double initialDebt;
    private double interestRate;
    private long initialDate;
    private String applicants;
    @JsonProperty("InterestRatesDetails")
    private List<Map<String, String>> interestRatesDetails;
    @JsonProperty("LoanDetails")
    private List<Map<String, String>> loanDetails;
    @JsonProperty("LoanNumber")
    private String loanNumber;
    @JsonProperty("PresentDebt")
    private String presentDebt;
    @JsonProperty("Type")
    private String type;

    @JsonIgnore
    private Map<String, String> transformedLoanDetails;

    public String getLoanNumber() {
        return loanNumber;
    }

    public String getType() {
        return type;
    }

    public String getPresentDebt() {
        return presentDebt;
    }

    public List<Map<String, String>> getLoanDetails() {
        return loanDetails;
    }

    public void setLoanDetails(List<Map<String, String>> loanDetails) {
        this.loanDetails = loanDetails;
        this.transformedLoanDetails = buildLoanDetailsMap();
    }

    @JsonIgnore
    private Map<String, String> buildLoanDetailsMap() {
        Map<String, String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : loanDetails) {
            map.put(keyValuePair.get(IcaBankenConstants.IdTags.KEY_TAG).toLowerCase().trim(),
                    keyValuePair.get(IcaBankenConstants.IdTags.VALUE_TAG));
        }

        return map;
    }

    @JsonIgnore
    public LoanAccount toTinkLoan() {
        IcaBankenLoanParsingHelper loanParsingHelper = new IcaBankenLoanParsingHelper(transformedLoanDetails);

        return LoanAccount.builder(loanNumber, loanParsingHelper.getBalance(presentDebt))
                .setAccountNumber(loanNumber)
                .setName(loanName)
                .setHolderName(loanParsingHelper.getLoanHolderName())
                .setBankIdentifier(loanNumber)
                .setInterestRate(interestRate)
                .setDetails(buildLoanDetails(loanParsingHelper))
                .build();
    }

    @JsonIgnore
    private LoanDetails buildLoanDetails(IcaBankenLoanParsingHelper loanParsingHelper) {
        logLoanType();

        return LoanDetails.builder(LoanDetails.Type.BLANCO)
                .setApplicants(loanParsingHelper.getApplicantsList())
                .setAmortized(loanParsingHelper.getAmortized(presentDebt))
                .setInitialBalance(loanParsingHelper.getInitialBalance())
                .setInitialDate(loanParsingHelper.getInitialDate())
                .setCoApplicant(loanParsingHelper.hasCoApplicant())
                .build();
    }

    private void logLoanType() {
        log.info("Unknown loan type: Name: {}, Type: {}",
                Optional.ofNullable(loanName).orElse("Not present"),
                Optional.ofNullable(type).orElse("Not present"));
    }
}
