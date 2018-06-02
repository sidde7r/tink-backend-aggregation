package se.tink.backend.common.mapper;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Loan;

public class CoreLoanMapper {
    @VisibleForTesting
    static final TypeMap<se.tink.backend.system.rpc.Loan, Loan> systemLoanMap = new ModelMapper()
            .createTypeMap(se.tink.backend.system.rpc.Loan.class, Loan.class);

    public static Loan toCoreLoan(se.tink.backend.system.rpc.Loan loan) {
        return systemLoanMap.map(loan);
    }
}
