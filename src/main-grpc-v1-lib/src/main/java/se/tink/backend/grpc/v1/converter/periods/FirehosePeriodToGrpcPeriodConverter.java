package se.tink.backend.grpc.v1.converter.periods;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.grpc.v1.models.Periods;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.Period;

public class FirehosePeriodToGrpcPeriodConverter {
    private final CorePeriodToGrpcPeriodConverter periodToGrpcConverter = new CorePeriodToGrpcPeriodConverter();

    public Periods convertFrom(List<se.tink.backend.firehose.v1.models.Period> periods) {
        Periods.Builder periodsBuilder = Periods.newBuilder();
        Map<String, Period> map = Maps.newHashMap();
        for (se.tink.backend.firehose.v1.models.Period period : periods) {
            se.tink.libraries.date.Period corePeriod = new se.tink.libraries.date.Period();
            ConverterUtils.setIfPresent(period::getClean, corePeriod::setClean);
            ConverterUtils.setIfPresent(period::getStartDate, corePeriod::setStartDate, Date::new);
            ConverterUtils.setIfPresent(period::getEndDate, corePeriod::setEndDate, Date::new);
            ConverterUtils.setIfPresent(period::getName, corePeriod::setName);
            ConverterUtils.setIfPresent(period::getResolution, corePeriod::setResolution,
                    resolution -> EnumMappers.FIREHOSE_RESOLUTION_TYPE_TO_CORE_MAP
                            .getOrDefault(resolution, ResolutionTypes.ALL));
            map.put(period.getName(), periodToGrpcConverter.convertFrom(corePeriod));
        }

        return periodsBuilder.putAllPeriod(map).build();
    }
}
