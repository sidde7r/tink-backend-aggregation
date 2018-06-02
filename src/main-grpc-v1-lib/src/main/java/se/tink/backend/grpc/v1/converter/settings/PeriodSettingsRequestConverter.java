package se.tink.backend.grpc.v1.converter.settings;

import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.UpdatePeriodSettingsCommand;
import se.tink.grpc.v1.rpc.UpdatePeriodSettingsRequest;

public class PeriodSettingsRequestConverter implements
        Converter<UpdatePeriodSettingsRequest, UpdatePeriodSettingsCommand> {

    @Override
    public UpdatePeriodSettingsCommand convertFrom(UpdatePeriodSettingsRequest input) {
        Integer monthlyAdjustedDay =
                !input.hasMonthlyAdjustedDay() ? null : input.getMonthlyAdjustedDay().getValue();
        ResolutionTypes mode = EnumMappers.CORE_RESOLUTION_TYPE_TO_PERIOD_SETTINGS_GRPC_MAP.inverse()
                .get(input.getPeriodDateBreakType());
        return new UpdatePeriodSettingsCommand(monthlyAdjustedDay, mode);
    }
}
