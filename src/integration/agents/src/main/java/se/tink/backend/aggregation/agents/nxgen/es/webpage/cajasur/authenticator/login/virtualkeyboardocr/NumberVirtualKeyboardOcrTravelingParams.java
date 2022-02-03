package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import lombok.Getter;

public class NumberVirtualKeyboardOcrTravelingParams {
    private static final int NUMBER_BUTTONS_IN_ROW = 5;
    @Getter private int horizontalVal;
    @Getter private int verticalVal;
    private int numberButtonInRowCounter = 1;
    private final VirtualKeyboardImageParameters virtualKeyboardImageParameters;

    public NumberVirtualKeyboardOcrTravelingParams(
            VirtualKeyboardImageParameters virtualKeyboardImageParameters) {
        this.virtualKeyboardImageParameters = virtualKeyboardImageParameters;
        horizontalVal = virtualKeyboardImageParameters.getFirstRowHorizontalInit();
        verticalVal = virtualKeyboardImageParameters.getFirstRowVerticalInit();
    }

    void computeNextNumberPosition() {
        horizontalVal += virtualKeyboardImageParameters.getHorizontalStep();
        if (numberButtonInRowCounter == NUMBER_BUTTONS_IN_ROW) {
            horizontalVal = virtualKeyboardImageParameters.getFirstRowHorizontalInit();
            verticalVal += virtualKeyboardImageParameters.getVerticalStep();
            numberButtonInRowCounter = 1;
        } else {
            numberButtonInRowCounter++;
        }
    }
}
