package se.tink.backend.grpc.v1.converter.periods;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.grpc.v1.models.Periods;
import se.tink.libraries.date.Period;

public class PeriodsGrpcConverter {

    private CorePeriodToGrpcPeriodConverter corePeriodToGrpcPeriodConverter;

    public PeriodsGrpcConverter(
            CorePeriodToGrpcPeriodConverter corePeriodToGrpcPeriodConverter) {
        this.corePeriodToGrpcPeriodConverter = corePeriodToGrpcPeriodConverter;
    }

    public Periods convertFrom(List<Period> input) {
        Map<String, se.tink.grpc.v1.models.Period> periodsMap = input.stream().collect(
                Collectors.toMap(Period::getName, corePeriodToGrpcPeriodConverter::convertFrom));
        return Periods.newBuilder().putAllPeriod(periodsMap).build();
    }
}
