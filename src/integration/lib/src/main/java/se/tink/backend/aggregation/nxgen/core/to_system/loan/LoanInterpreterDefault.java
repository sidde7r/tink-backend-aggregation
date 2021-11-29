package se.tink.backend.aggregation.nxgen.core.to_system.loan;

import com.google.common.collect.ImmutableSet;

public class LoanInterpreterDefault extends LoanInterpreter {

    private static ImmutableSet<NamePart> EMPTY_NAME_SET = ImmutableSet.of();

    protected LoanInterpreterDefault() {
        super(EMPTY_NAME_SET);
    }
}
