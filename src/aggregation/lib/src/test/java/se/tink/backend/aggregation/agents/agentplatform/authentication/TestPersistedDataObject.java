package se.tink.backend.aggregation.agents.agentplatform.authentication;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import org.junit.Ignore;

@EqualsAndHashCode
@Ignore
public class TestPersistedDataObject implements Serializable {

    private String stringParam;

    private Integer integerValue;

    public TestPersistedDataObject(String stringParam, Integer integerValue) {
        this.stringParam = stringParam;
        this.integerValue = integerValue;
    }

    public String getStringParam() {
        return stringParam;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }
}
