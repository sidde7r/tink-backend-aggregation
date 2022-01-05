package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class VirtualKeyboardImageParameters {

    private int imgWidth;
    private int imgHeight;
    private int firstRowHorizontalInit;
    private int firstRowVerticalInit;
    private int horizontalStep;
    private int verticalStep;

    public static VirtualKeyboardImageParameters createWebsitePersonalConfiguration() {
        VirtualKeyboardImageParameters instance = new VirtualKeyboardImageParameters();
        instance.imgWidth = 9;
        instance.imgHeight = 13;
        instance.firstRowHorizontalInit = 7;
        instance.firstRowVerticalInit = 4;
        instance.horizontalStep = 34;
        instance.verticalStep = 34;
        return instance;
    }

    public static VirtualKeyboardImageParameters createMobilePersonalConfiguration() {
        VirtualKeyboardImageParameters instance = new VirtualKeyboardImageParameters();
        instance.imgWidth = 11;
        instance.imgHeight = 13;
        instance.firstRowHorizontalInit = 11;
        instance.firstRowVerticalInit = 9;
        instance.horizontalStep = 34;
        instance.verticalStep = 34;
        return instance;
    }

    public static VirtualKeyboardImageParameters createEnterpriseConfiguration() {
        VirtualKeyboardImageParameters instance = new VirtualKeyboardImageParameters();
        instance.imgWidth = 8;
        instance.imgHeight = 12;
        instance.firstRowHorizontalInit = 8;
        instance.firstRowVerticalInit = 5;
        instance.horizontalStep = 28;
        instance.verticalStep = 28;
        return instance;
    }
}
