package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.Collections;
import java.util.Map;

public class HeaderValueEntity {
    private String header;
    private String value;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> asKeyValueMap() {
        return Collections.singletonMap(header, value);
    }
}
