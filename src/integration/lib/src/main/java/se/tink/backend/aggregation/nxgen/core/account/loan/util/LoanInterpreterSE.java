package se.tink.backend.aggregation.nxgen.core.account.loan.util;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class LoanInterpreterSE extends LoanInterpreter {

    private static ImmutableSet<NamePart> NAME_PARTS;

    static {
        NAME_PARTS =
                ImmutableSet.of(

                        // These "probabilities" are highly un-scientific
                        // It's mostly my feeling (i have verified on real data)

                        // MORTGAGES
                        new LoanInterpreter.NamePart("bolån", 1.0, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart(
                                "stadshypotek", 1.0, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("hypotekslån", 0.9, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("hypotek", 0.7, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("botten", 0.7, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("villa", 0.4, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("hus", 0.4, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("bostad", 0.6, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("boende", 0.6, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("borätt", 0.5, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("bo-lån", 0.6, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("landet", 0.5, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("fastighet", 0.4, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("takräntelån", 0.7, LoanDetails.Type.MORTGAGE),
                        new LoanInterpreter.NamePart("laholmslån", 1.0, LoanDetails.Type.MORTGAGE),
                        // for example "Bundet Xmån Prv"
                        new LoanInterpreter.NamePart("mån prv", 0.8, LoanDetails.Type.MORTGAGE),

                        // BLANCO
                        new LoanInterpreter.NamePart("topplån", 0.7, LoanDetails.Type.BLANCO),
                        new LoanInterpreter.NamePart("handelsbanken", 1.0, LoanDetails.Type.BLANCO),
                        new LoanInterpreter.NamePart("blanco", 1.0, LoanDetails.Type.BLANCO),
                        new LoanInterpreter.NamePart("privat", 0.8, LoanDetails.Type.BLANCO),

                        // MEMBERSHIP
                        new LoanInterpreter.NamePart(
                                "medlemslån", 0.5, LoanDetails.Type.MEMBERSHIP),

                        // VEHICLE
                        new LoanInterpreter.NamePart("bil", 0.7, LoanDetails.Type.VEHICLE),
                        new LoanInterpreter.NamePart("båt", 0.7, LoanDetails.Type.VEHICLE),

                        // LAND
                        new LoanInterpreter.NamePart("skog", 0.9, LoanDetails.Type.LAND),
                        new LoanInterpreter.NamePart("jordbruk", 0.9, LoanDetails.Type.LAND),
                        new LoanInterpreter.NamePart("mark", 0.9, LoanDetails.Type.LAND),
                        new LoanInterpreter.NamePart("lantbruk", 0.9, LoanDetails.Type.LAND),

                        // STUDENT
                        new LoanInterpreter.NamePart("studie", 0.8, LoanDetails.Type.STUDENT),
                        new LoanInterpreter.NamePart("student", 0.8, LoanDetails.Type.STUDENT),
                        new LoanInterpreter.NamePart("utbildning", 0.8, LoanDetails.Type.STUDENT));
    }

    public LoanInterpreterSE() {
        super(NAME_PARTS);
    }
}
