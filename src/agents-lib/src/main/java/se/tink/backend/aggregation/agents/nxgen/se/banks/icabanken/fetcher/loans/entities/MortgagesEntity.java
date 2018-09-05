package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Maps;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
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
public class MortgagesEntity {
    private boolean coApplicants;
    private static final Splitter AND_SPLITTER = Splitter.on(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR)
            .trimResults();
    private static final Splitter SPLITTER = Splitter.on(CharMatcher.breakingWhitespace());

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
                transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.INTEREST_RATE));
    }

    public Double getInitialDebt() {
        String initialDebt = transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.INITIAL_DEBT);
        if (!Strings.isNullOrEmpty(initialDebt)) {
            return AgentParsingUtils.parseAmountTrimCurrency(initialDebt);
        }
        return null;
    }

    public Date getNextDayOfTermsChange() throws ParseException {
        String nextDatOfTermsChange = transformedMortgageDetails.get(
                IcaBankenConstants.IcaMessages.NEXT_DAY_OF_TERMS_CHANGE);
        if (!Strings.isNullOrEmpty(nextDatOfTermsChange)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(nextDatOfTermsChange);
        }
        return null;
    }

    public Integer getNumMonthsBound() {
        String periodDescription = transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.MONTH_BOUND);
        if (!Strings.isNullOrEmpty(periodDescription)) {
            String cleanPeriodDescription = periodDescription.replace("-", " ");

            Iterable<String> timeDescription = SPLITTER.split(cleanPeriodDescription);
            if (Iterables.size(timeDescription) == 3) {
                return AgentParsingUtils.parseNumMonthsBound(
                        Iterables.get(timeDescription, 1) + " " + Iterables.get(timeDescription, 2));
            }
        }
        return null;
    }

    public String getSecurity() {
        return transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.SECURITY);
    }

    public String getTypeOfSecurity() {
        return transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.TYPE_OF_SECURITY);
    }

    public String getTypeOfLoan() {
        return transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.TYPE_OF_LOAN);
    }

    public String getApplicants() {
        return transformedMortgageDetails.get(IcaBankenConstants.IcaMessages.APPLICANTS);
    }

    public List<Map<String, String>> getMortgageDetails() {
        return this.mortgageDetails;
    }

    public void setMortgageDetails(List<Map<String, String>> mortgageDetails) {
        this.mortgageDetails = mortgageDetails;

        Map<String, String> map = Maps.newHashMap();

        for (Map<String, String> keyValuePair : mortgageDetails) {
            map.put(keyValuePair.get(IcaBankenConstants.IdTags.KEY_TAG),
                    keyValuePair.get(IcaBankenConstants.IdTags.VALUE_TAG));
        }

        this.transformedMortgageDetails = map;
    }

    public LoanAccount toTinkLoan() {
        try {
            return LoanAccount.builder(getMortgageNumber(),
                    toAmount(-AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt())))
                    .setAccountNumber(getMortgageNumber())
                    .setName(getTypeOfLoan())
                    .setBankIdentifier(getMortgageNumber())
                    .setInterestRate(getInterestRate())
                    .setDetails(LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                            .setNextDayOfTermsChange(getNextDayOfTermsChange())
                            .setNumMonthsBound(getNumMonthsBound())
                            .setInitialBalance(toAmount(-getInitialDebt()))
                            .setAmortized(toAmount(
                                    -AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt()) + getInitialDebt()))
                            .setApplicants(ApplicantHelper(getApplicants()))
                            .setSecurity(getSecurity())
                            .build())

                    .build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return LoanAccount.builder(getMortgageNumber(),
                toAmount(-AgentParsingUtils.parseAmountTrimCurrency(getPresentDebt())))
                .setAccountNumber(getMortgageNumber())
                .build();
    }

    public Amount toAmount(Double amount) {
        return Amount.inSEK(amount);
    }

    private List<String> ApplicantHelper(String applicants) {
        List<String> applicantsAsList = new ArrayList<>();

        Preconditions.checkState(!Strings.isNullOrEmpty(applicants), "Applicants is null");
        if (applicants.contains(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR)) {
            Iterable<String> multipleApplicants = AND_SPLITTER.split(applicants);
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


