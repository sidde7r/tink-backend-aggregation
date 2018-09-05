package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Comparator;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails.Type;

public abstract class LoanInterpreter implements Serializable {
    private static final String MARKET_SE = "SE";

    private ImmutableSet<NamePart> nameParts;

    protected LoanInterpreter(ImmutableSet<NamePart> nameParts) {
        this.nameParts = nameParts;
    }

    public static LoanInterpreter getInstance(String market) {
        if (MARKET_SE.equalsIgnoreCase(market)) {
            return new LoanInterpreterSE();
        }

        return new LoanInterpreterDefault();
    }

    public Type interpretLoanType(final String loanName) {
        return nameParts.stream()
                .filter(namePart -> loanName.toLowerCase().contains(namePart.getPartInLowerCase()))
                .max(Comparator.comparingDouble(NamePart::getProbability))
                .map(NamePart::getType)
                .orElse(Type.OTHER);
    }

    protected static class NamePart {
        double probability;
        private String part;
        private Type type;

        public NamePart(String part, double probability, Type type) {
            this.part = part;
            this.probability = probability;
            this.type = type;
        }

        public String getPartInLowerCase() {
            return part.toLowerCase();
        }

        public Type getType() {
            return type;
        }

        public double getProbability() {
            return probability;
        }
    }
}
