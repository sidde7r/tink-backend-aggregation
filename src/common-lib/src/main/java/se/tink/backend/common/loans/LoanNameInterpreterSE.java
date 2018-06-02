package se.tink.backend.common.loans;

import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import se.tink.backend.core.Loan;

public class LoanNameInterpreterSE extends LoanNameInterpreter {

    private static final ImmutableSet<String> KEYWORDS_VARIABLE_RATE = ImmutableSet.of("rörlig");
    private static final Pattern PATTERN_MONTHS_BOUND = Pattern.compile("(\\d)+ ?-?mån");
    private static final Pattern PATTERN_YEARS_BOUND = Pattern.compile("(\\d)+ ?-?år");
    private static ImmutableSet<LoanNameInterpreter.NamePart> NAME_PARTS;

    static {
        NAME_PARTS = ImmutableSet.of(

            // These "probabilities" are highly un-scientific
            // It's mostly my feeling (i have verified on real data)

            // MORTGAGES
            new LoanNameInterpreter.NamePart("bolån", 1.0, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("stadshypotek", 1.0, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("hypotekslån", 0.9, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("hypotek", 0.7, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("botten", 0.7, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("villa", 0.4, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("hus", 0.4, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("bostad", 0.6, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("boende", 0.6, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("borätt", 0.5, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("bo-lån", 0.6, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("landet", 0.5, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("fastighet", 0.4, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("takräntelån", 0.7, Loan.Type.MORTGAGE),
            new LoanNameInterpreter.NamePart("laholmslån", 1.0, Loan.Type.MORTGAGE),
                // for example "Bundet Xmån Prv"
            new LoanNameInterpreter.NamePart("mån prv", 0.8, Loan.Type.MORTGAGE),

            // BLANCO
            new LoanNameInterpreter.NamePart("topplån", 0.7, Loan.Type.BLANCO),
            new LoanNameInterpreter.NamePart("handelsbanken", 1.0, Loan.Type.BLANCO),
            new LoanNameInterpreter.NamePart("blanco", 1.0, Loan.Type.BLANCO),
            new LoanNameInterpreter.NamePart("privat", 0.8, Loan.Type.BLANCO),

            // MEMBERSHIP
            new LoanNameInterpreter.NamePart("medlemslån", 0.5, Loan.Type.MEMBERSHIP),

            // VEHICLE
            new LoanNameInterpreter.NamePart("bil", 0.7, Loan.Type.VEHICLE),
            new LoanNameInterpreter.NamePart("båt", 0.7, Loan.Type.VEHICLE),

            // LAND
            new LoanNameInterpreter.NamePart("skog", 0.9, Loan.Type.LAND),
            new LoanNameInterpreter.NamePart("jordbruk", 0.9, Loan.Type.LAND),
            new LoanNameInterpreter.NamePart("mark", 0.9, Loan.Type.LAND),
            new LoanNameInterpreter.NamePart("lantbruk", 0.9, Loan.Type.LAND),

            // STUDENT
            new LoanNameInterpreter.NamePart("studie", 0.8, Loan.Type.STUDENT),
            new LoanNameInterpreter.NamePart("student", 0.8, Loan.Type.STUDENT),
            new LoanNameInterpreter.NamePart("utbildning", 0.8, Loan.Type.STUDENT)
        );
    }

    public LoanNameInterpreterSE(String loanName) {
        super(NAME_PARTS, KEYWORDS_VARIABLE_RATE, PATTERN_MONTHS_BOUND, PATTERN_YEARS_BOUND);
        processLoanName(loanName);
    }
}
