package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Maps;
import com.google.common.base.Splitter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class LoansEntity {
    private static final Splitter SPLITTER = Splitter.on(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR).trimResults();
    private boolean coApplicants;

    @JsonProperty("InterestRatesDetails")
    private List<Map<String, String>> interestRatesDetails;

    @JsonProperty("LoanDetails")
    private List<Map<String, String>> loanDetails;

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
        return AgentParsingUtils.parsePercentageFormInterest(
                transformedLoanDetails.get(IcaBankenConstants.IcaMessages.INTEREST_RATE));
    }

    public String getLoanName() {
        return transformedLoanDetails.get(IcaBankenConstants.IcaMessages.LOAN_NAME);
    }

    public Double getInitialDebt() {
        String initialDebt = transformedLoanDetails.get(IcaBankenConstants.IcaMessages.INITIAL_DEBT);
        if (!Strings.isNullOrEmpty(initialDebt)) {
            return AgentParsingUtils.parseAmountTrimCurrency(initialDebt);
        }
        return null;
    }

    public void setLoanDetails(List<Map<String, String>> loanDetails) {
        this.loanDetails = loanDetails;

        Map<String, String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : loanDetails) {
            map.put(keyValuePair.get(IcaBankenConstants.IdTags.KEY_TAG),
                    keyValuePair.get(IcaBankenConstants.IdTags.VALUE_TAG));
        }

        this.transformedLoanDetails = map;
    }

    public Date getInitialDate() {
        String initialDate = transformedLoanDetails.get(IcaBankenConstants.IcaMessages.INITIAL_DATE);
        if (!Strings.isNullOrEmpty(initialDate)) {
            try {
                return ThreadSafeDateFormat.FORMATTER_DAILY.parse(initialDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getApplicants() {
        return transformedLoanDetails.get(IcaBankenConstants.IcaMessages.APPLICANTS);
    }

    public List<Map<String, String>> getLoanDetails() {
        return this.loanDetails;
    }

    public LoanAccount toTinkLoan() {

        return LoanAccount.builder(getLoanNumber(),
                toAmount(-AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt())))
                .setAccountNumber(getLoanNumber())
                .setName(getLoanName())
                .setBankIdentifier(getLoanNumber())
                .setInterestRate(getInterestRate())
                .setDetails(LoanDetails.builder()
                        .setType(LoanDetails.Type.BLANCO)
                        .setInitialDate(getInitialDate())
                        .setInitialBalance(toAmount(-getInitialDebt()))
                        .setAmortized(toAmount(
                                -AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt()) + getInitialDebt()))
                        .setApplicants(ApplicantHelper(getApplicants()))
                        .build())
                .build();
    }

    public Amount toAmount(Double amount) {
        return Amount.inSEK(amount);
    }

    private List<String> ApplicantHelper(String applicants) {
        List<String> applicantsAsList = new ArrayList<>();

        Preconditions.checkState(!Strings.isNullOrEmpty(applicants), "Applicants is null");
        if (applicants.contains(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR)) {
            Iterable<String> multipleApplicants = SPLITTER.split(applicants);
            for (String applicant : multipleApplicants) {
                applicantsAsList.add(applicant);
            }
            this.coApplicants = true;
            return applicantsAsList;
        } else {
            applicantsAsList.add(applicants);
            this.coApplicants = false;
            return applicantsAsList;
        }
    }
}
