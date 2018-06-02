package se.tink.backend.grpc.v1.converter.settings;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.PeriodDateBreakType;
import se.tink.grpc.v1.models.PeriodSettings;

public class CorePeriodSettingsToGrpcConverter implements
        Converter<se.tink.backend.core.PeriodSettings, PeriodSettings> {

    @Override
    public PeriodSettings convertFrom(se.tink.backend.core.PeriodSettings input) {
        PeriodSettings.Builder builder = PeriodSettings.newBuilder();
        ConverterUtils.setIfPresent(input::getMode, builder::setPeriodDateBreakType, mode ->
            EnumMappers.CORE_RESOLUTION_TYPE_TO_PERIOD_SETTINGS_GRPC_MAP
                    .getOrDefault(mode, PeriodDateBreakType.PERIOD_DATE_BREAK_TYPE_UNKNOWN
        ));
        ConverterUtils.setIfPresent(input::getAdjustedPeriodDay, builder::setMonthlyAdjustedDay);
        return builder.build();
    }
}
