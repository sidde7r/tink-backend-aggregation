package se.tink.backend.common.loans;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.core.Loan;

public abstract class LoanNameInterpreter implements Serializable {
    private static final String MARKET_SE = "SE";

    private ImmutableSet<String> keywordsVariableRate;
    private Pattern patternMonthsBound;
    private Pattern patternYearsBound;

    private ImmutableSet<NamePart> nameParts;
    private Integer numMonthsBound;
    private Loan.Type loanType;

    protected LoanNameInterpreter(ImmutableSet<NamePart> nameParts, ImmutableSet<String> keywordsVariableRate,
            Pattern patternMonthsBound, Pattern patternYearsBound) {
        this.nameParts = nameParts;
        this.keywordsVariableRate = keywordsVariableRate;
        this.patternMonthsBound = patternMonthsBound;
        this.patternYearsBound = patternYearsBound;
    }

    public static LoanNameInterpreter getInstance(String market, String loanName) {
        if (MARKET_SE.equalsIgnoreCase(market)) {
            return new LoanNameInterpreterSE(loanName);
        }

        throw new RuntimeException(String.format("Market[%s] is not supported", market));
    }

    public Loan.Type getGuessedLoanType() {
        return loanType;
    }

    public Integer getGuessedNumMonthsBound() {
        return numMonthsBound;
    }

    protected void processLoanName(String loanName) {
        loanType = interpretLoanType(loanName);
        numMonthsBound = interpretNumMonthsBound(loanName);
    }

    private Loan.Type interpretLoanType(final String loanName) {
        return nameParts.stream()
                .filter(namePart -> loanName.toLowerCase().contains(namePart.getPartInLowerCase()))
                .max(Comparator.comparingDouble(NamePart::getProbability))
                .map(NamePart::getType)
                .orElse(Loan.Type.OTHER);
    }

    private Integer interpretNumMonthsBound(final String loanName) {

        Matcher mMonth = patternMonthsBound.matcher(loanName.toLowerCase());
        if (mMonth.find()) {
            try {
                return Integer.parseInt(mMonth.group(1));
            } catch (Exception e) {
                //something weird
            }
        }
        Matcher mYear = patternYearsBound.matcher(loanName.toLowerCase());
        if (mYear.find()) {
            try {
                return Integer.parseInt(mYear.group(1)) * 12;
            } catch (Exception e) {
                //something weird
            }
        }

        boolean isVariable = keywordsVariableRate.stream()
                .anyMatch(keyword -> loanName.toLowerCase().contains(keyword.toLowerCase()));
        if (isVariable) {
            return 3;
        } else {
            return null;
        }
    }

    protected static class NamePart {
        double probability;
        private String part;
        private Loan.Type type;

        public NamePart(String part, double probability, Loan.Type type) {
            this.part = part;
            this.probability = probability;
            this.type = type;
        }

        public String getPartInLowerCase() {
            return part.toLowerCase();
        }

        public Loan.Type getType() {
            return type;
        }

        public double getProbability() {
            return probability;
        }
    }
}
