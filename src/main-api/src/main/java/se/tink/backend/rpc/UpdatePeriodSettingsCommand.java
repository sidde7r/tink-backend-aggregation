package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import se.tink.libraries.date.ResolutionTypes;

public class UpdatePeriodSettingsCommand {
    private Integer monthlyAdjustedDay;
    private ResolutionTypes mode;

    private static final ImmutableList<ResolutionTypes> acceptableResolutionTypes = ImmutableList.<ResolutionTypes>builder()
            .add(ResolutionTypes.MONTHLY)
            .add(ResolutionTypes.MONTHLY_ADJUSTED)
            .build();

    public UpdatePeriodSettingsCommand() {
    }

    public UpdatePeriodSettingsCommand(Integer monthlyAdjustedDay, ResolutionTypes mode) {
        validate(monthlyAdjustedDay, mode);
        this.monthlyAdjustedDay = monthlyAdjustedDay;
        this.mode = mode;
    }

    private void validate(Integer monthlyAdjustedDay, ResolutionTypes mode) {
        Preconditions.checkArgument(validateMonthlyAdjustedDay(monthlyAdjustedDay));

        Preconditions.checkArgument(mode == null || acceptableResolutionTypes.contains(mode));
    }

    public Integer getMonthlyAdjustedDay() {
        return monthlyAdjustedDay;
    }

    public ResolutionTypes getMode() {
        return mode;
    }

    public boolean validateMonthlyAdjustedDay(Integer value) {
        if (value == null) {
            return true;
        }

        if (value < 31 && value >=0) {
            return true;
        }

        return false;
    }
}
