package se.tink.backend.core;

import se.tink.libraries.date.ResolutionTypes;

public class PeriodSettings {
    private ResolutionTypes mode;
    private int adjustedPeriodDay;

    public PeriodSettings(ResolutionTypes mode, int adjustedPeriodDay) {
        this.mode = mode;
        this.adjustedPeriodDay = adjustedPeriodDay;
    }

    public ResolutionTypes getMode() {
        return mode;
    }

    public int getAdjustedPeriodDay() {
        return adjustedPeriodDay;
    }

}
