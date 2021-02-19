package se.tink.backend.aggregation.nxgen.controllers.configuration.configuration;

import org.junit.Ignore;
import se.tink.backend.aggregation.annotations.JsonObject;

@Ignore
@JsonObject
public class OuterConfiguration {
    private String stringLevel1;
    private int integerLevel1;
    private NestedConfigurationLevel1 nestedConfigurationLevel1;

    public OuterConfiguration(
            String stringLevel1,
            int integerLevel1,
            NestedConfigurationLevel1 nestedConfigurationLevel1) {
        this.stringLevel1 = stringLevel1;
        this.integerLevel1 = integerLevel1;
        this.nestedConfigurationLevel1 = nestedConfigurationLevel1;
    }

    public String getStringLevel1() {
        return stringLevel1;
    }

    public void setStringLevel1(String stringLevel1) {
        this.stringLevel1 = stringLevel1;
    }

    public int getIntegerLevel1() {
        return integerLevel1;
    }

    public void setIntegerLevel1(int integerLevel1) {
        this.integerLevel1 = integerLevel1;
    }

    public NestedConfigurationLevel1 getNestedConfigurationLevel1() {
        return nestedConfigurationLevel1;
    }

    public void setNestedConfigurationLevel1(NestedConfigurationLevel1 nestedConfigurationLevel1) {
        this.nestedConfigurationLevel1 = nestedConfigurationLevel1;
    }
}
