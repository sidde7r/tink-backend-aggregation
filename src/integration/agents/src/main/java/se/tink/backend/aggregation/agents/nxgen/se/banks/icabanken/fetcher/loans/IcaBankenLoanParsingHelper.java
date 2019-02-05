package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils.IcaBankenFormatUtils;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class IcaBankenLoanParsingHelper {
    private final Map<String, String> loanDetailsMap;

    public IcaBankenLoanParsingHelper(Map<String, String> loanDetailsMap) {
        this.loanDetailsMap = loanDetailsMap;
    }

    public Amount getBalance(String presentDebt) {
        Preconditions.checkNotNull(presentDebt);
        return Amount.inSEK(-1.0 * AgentParsingUtils.parseAmountTrimCurrency(presentDebt));
    }

    public String getTypeOfLoan() {
        return loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.TYPE_OF_LOAN);
    }

    public String getLoanName() {
        return loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.LOAN_NAME);
    }

    public Double getInterestRate() {
        return AgentParsingUtils.parsePercentageFormInterest(
                loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.INTEREST_RATE));
    }

    public Date getNextDayOfTermsChange() {
        String nextDayOfTermsChange = loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.NEXT_DAY_OF_TERMS_CHANGE);

        if (Strings.isNullOrEmpty(nextDayOfTermsChange)) {
            return null;
        }

        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(nextDayOfTermsChange);
        } catch (ParseException e) {
            return null;
        }
    }

    public Date getInitialDate() {
        String initialDate = loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.INITIAL_DATE);

        if (Strings.isNullOrEmpty(initialDate)) {
            return null;
        }

        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(initialDate);
        } catch (ParseException e) {
            return null;
        }
    }

    public Amount getInitialBalance() {
        Double initialDebt = getInitialDebt();

        if (initialDebt == null) {
            return null;
        }

        return Amount.inSEK(-1.0 * initialDebt);
    }

    public Amount getAmortized(String presentDebt) {
        Double initialDebt = getInitialDebt();

        if (initialDebt == null) {
            return null;
        }

       return Amount.inSEK(initialDebt - AgentParsingUtils.parseAmountTrimCurrency(presentDebt));
    }

    private Double getInitialDebt() {
        String initialDebt = loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.INITIAL_DEBT);

        if (!Strings.isNullOrEmpty(initialDebt)) {
            return AgentParsingUtils.parseAmountTrimCurrency(initialDebt);
        }

        return null;
    }

    public HolderName getLoanHolderName() {
        List<String> applicantsList = getApplicantsList();

        if (applicantsList.isEmpty()) {
            return null;
        }

        return new HolderName(applicantsList.get(0));
    }

    public List<String> getApplicantsList() {
        String applicants = getApplicants();

        if (Strings.isNullOrEmpty(applicants)) {
            return Collections.emptyList();
        }

        if (applicants.contains(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR)) {
            return IcaBankenFormatUtils.AND_SPLITTER.splitToList(applicants);
        } else {
            return Collections.singletonList(applicants);
        }
    }

    private String getApplicants() {
        return loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.APPLICANTS);
    }

    public boolean hasCoApplicant() {
        String applicants = getApplicants();

        return !Strings.isNullOrEmpty(applicants) &&
                applicants.contains(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR);
    }

    public String getSecurity() {
        return loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.SECURITY);
    }

    public Integer getNumMonthsBound() {
        String periodDescription = loanDetailsMap.get(IcaBankenConstants.LoanDetailsKeys.MONTH_BOUND);

        if (!Strings.isNullOrEmpty(periodDescription)) {
            String cleanPeriodDescription = periodDescription.replace("-", " ");

            Iterable<String> timeDescription = IcaBankenFormatUtils.WHITESPACE_SPLITTER.split(cleanPeriodDescription);
            if (Iterables.size(timeDescription) == 3) {
                return AgentParsingUtils.parseNumMonthsBound(
                        Iterables.get(timeDescription, 1) + " " + Iterables.get(timeDescription, 2));
            }
        }
        return null;
    }
}
