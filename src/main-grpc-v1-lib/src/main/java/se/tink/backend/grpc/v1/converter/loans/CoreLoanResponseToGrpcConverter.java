package se.tink.backend.grpc.v1.converter.loans;

import java.util.stream.Collectors;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.rpc.ListLoansResponse;
import se.tink.libraries.uuid.UUIDUtils;

public class CoreLoanResponseToGrpcConverter implements Converter<LoanResponse, ListLoansResponse> {

    @Override
    public ListLoansResponse convertFrom(LoanResponse input) {
        ListLoansResponse.Builder builder = ListLoansResponse.newBuilder();

        if (input.getLoans() != null) {
            builder.addAllLoans(input.getLoans().stream().map(this::convert).collect(Collectors.toList()));
        }

        return builder.build();
    }

    private se.tink.grpc.v1.models.Loan convert(Loan input) {
        se.tink.grpc.v1.models.Loan.Builder builder = se.tink.grpc.v1.models.Loan.newBuilder();

        ConverterUtils.setIfPresent(input::getAccountId, builder::setAccountId, UUIDUtils::toTinkUUID);
        ConverterUtils.setIfPresent(input::getInterest, builder::setInterest, NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getNumMonthsBound, builder::setNumberOfMonthsBound);
        ConverterUtils.setIfPresent(input::getType, builder::setType, type -> EnumMappers.CORE_TO_GRPC_LOAN_TYPE_MAPPING
                .getOrDefault(type, se.tink.grpc.v1.models.Loan.Type.TYPE_UNKNOWN));

        return builder.build();
    }
}
