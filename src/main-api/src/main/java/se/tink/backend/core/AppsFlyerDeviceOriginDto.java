package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.protostuff.Tag;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppsFlyerDeviceOriginDto {
    @Tag(1)
    private String extraParam1;
    @Tag(2)
    private String extraParam2;
    @Tag(3)
    private String extraParam3;
    @Tag(4)
    private String extraParam4;
    @Tag(5)
    private String extraParam5;

    public String getExtraParam1() {
        return extraParam1;
    }

    public void setExtraParam1(String extraParam1) {
        this.extraParam1 = extraParam1;
    }

    public String getExtraParam2() {
        return extraParam2;
    }

    public void setExtraParam2(String extraParam2) {
        this.extraParam2 = extraParam2;
    }

    public String getExtraParam3() {
        return extraParam3;
    }

    public void setExtraParam3(String extraParam3) {
        this.extraParam3 = extraParam3;
    }

    public String getExtraParam4() {
        return extraParam4;
    }

    public void setExtraParam4(String extraParam4) {
        this.extraParam4 = extraParam4;
    }

    public String getExtraParam5() {
        return extraParam5;
    }

    public void setExtraParam5(String extraParam5) {
        this.extraParam5 = extraParam5;
    }
}
