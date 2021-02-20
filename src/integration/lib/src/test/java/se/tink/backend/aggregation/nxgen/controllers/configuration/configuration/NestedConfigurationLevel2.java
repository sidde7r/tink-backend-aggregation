package se.tink.backend.aggregation.nxgen.controllers.configuration.configuration;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Ignore;
import se.tink.backend.aggregation.annotations.JsonObject;

@Ignore
@JsonObject
public class NestedConfigurationLevel2 {
    private String stringLevel3;
    private int integerLevel3;
    private NestedConfigurationLevel2 nestedConfigurationLevel2;
    private List<NestedConfigurationLevel2> listNestedConfigurationLevel2;

    public NestedConfigurationLevel2(
            String stringLevel3,
            int integerLevel3,
            List<NestedConfigurationLevel2> listNestedConfigurationLevel2) {
        this.stringLevel3 = stringLevel3;
        this.integerLevel3 = integerLevel3;
        this.listNestedConfigurationLevel2 = ImmutableList.copyOf(listNestedConfigurationLevel2);
    }

    public NestedConfigurationLevel2(String stringLevel3, int integerLevel3) {
        this.stringLevel3 = stringLevel3;
        this.integerLevel3 = integerLevel3;
    }

    public List<NestedConfigurationLevel2> getListNestedConfigurationLevel2() {
        return listNestedConfigurationLevel2 == null
                ? null
                : ImmutableList.copyOf(listNestedConfigurationLevel2);
    }

    public void setListNestedConfigurationLevel2(
            List<NestedConfigurationLevel2> listNestedConfigurationLevel2) {
        this.listNestedConfigurationLevel2 = ImmutableList.copyOf(listNestedConfigurationLevel2);
    }

    public String getStringLevel3() {
        return stringLevel3;
    }

    public void setStringLevel3(String stringLevel3) {
        this.stringLevel3 = stringLevel3;
    }

    public int getIntegerLevel3() {
        return integerLevel3;
    }

    public void setIntegerLevel3(int integerLevel3) {
        this.integerLevel3 = integerLevel3;
    }

    public NestedConfigurationLevel2 getNestedConfigurationLevel2() {
        return nestedConfigurationLevel2;
    }

    public void setNestedConfigurationLevel2(NestedConfigurationLevel2 nestedConfigurationLevel2) {
        this.nestedConfigurationLevel2 = nestedConfigurationLevel2;
    }
}
