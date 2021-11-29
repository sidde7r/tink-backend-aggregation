package se.tink.backend.aggregation.nxgen.core.to_system.loan;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.enums.MarketCode;

public abstract class LoanInterpreter {

    private final ImmutableSet<NamePart> nameParts;

    protected LoanInterpreter(ImmutableSet<NamePart> nameParts) {
        this.nameParts = nameParts;
    }

    public static LoanInterpreter getInstance(MarketCode market) {

        if (MarketCode.SE.equals(market)) {
            return new LoanInterpreterSE();
        }

        return new LoanInterpreterDefault();
    }

    public Type interpretLoanType(final String loanName) {
        if (Strings.isNullOrEmpty(loanName)) {
            return Type.OTHER;
        }

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
