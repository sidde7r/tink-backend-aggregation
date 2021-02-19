package se.tink.backend.aggregation.nxgen.controllers.configuration.configuration;

import org.junit.Ignore;
import se.tink.backend.aggregation.annotations.JsonObject;

@Ignore
@JsonObject
public class NestedConfigurationLevel1 {
    private String stringLevel2;
    private int integerLevel2;
    private NestedConfigurationLevel2 nestedConfigurationLevel2;

    public NestedConfigurationLevel1(
            String stringLevel2,
            int integerLevel2,
            NestedConfigurationLevel2 nestedConfigurationLevel2) {
        this.stringLevel2 = stringLevel2;
        this.integerLevel2 = integerLevel2;
        this.nestedConfigurationLevel2 = nestedConfigurationLevel2;
    }

    public String getStringLevel2() {
        return stringLevel2;
    }

    public void setStringLevel2(String stringLevel2) {
        this.stringLevel2 = stringLevel2;
    }

    public int getIntegerLevel2() {
        return integerLevel2;
    }

    public void setIntegerLevel2(int integerLevel2) {
        this.integerLevel2 = integerLevel2;
    }

    public NestedConfigurationLevel2 getNestedConfigurationLevel2() {
        return nestedConfigurationLevel2;
    }

    public void setNestedConfigurationLevel2(NestedConfigurationLevel2 nestedConfigurationLevel2) {
        this.nestedConfigurationLevel2 = nestedConfigurationLevel2;
    }
}
