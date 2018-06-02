package se.tink.backend.grpc.v1.converter.periods;

import java.util.Objects;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.Period;

public class CorePeriodToGrpcPeriodConverter implements Converter<se.tink.libraries.date.Period, Period> {
    @Override
    public Period convertFrom(se.tink.libraries.date.Period period) {
        Period.Builder builder = Period.newBuilder()
                .setStart(ProtobufModelUtils.toProtobufTimestamp(period.getStartDate().getTime()))
                .setStop(ProtobufModelUtils.toProtobufTimestamp(period.getEndDate().getTime()));

        if (period.getResolution() == null || Objects.equals(period.getResolution(), ResolutionTypes.ALL)) {
            return builder.build();
        }

        builder.setYear(Integer.valueOf(period.getName().substring(0, 4)));
        String periodWithoutYear = period.getName().substring(Math.min(5, period.getName().length())); // year + separator

        switch (period.getResolution()) {
        case WEEKLY:
            builder.setWeek(Integer.valueOf(periodWithoutYear));
            break;
        case DAILY:
            builder.setDay(Integer.valueOf(periodWithoutYear.substring(periodWithoutYear.length() - 2)));
            // fall through
        case MONTHLY:
        case MONTHLY_ADJUSTED:
            builder.setMonth(Integer.valueOf(periodWithoutYear.substring(0, 2)));
            break;
        default:
        }

        return builder.build();
    }
}
