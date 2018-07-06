package se.tink.backend.aggregation.nxgen.http;

import java.util.HashMap;
import java.util.Map;

public class FormBuilder {

    private static final String DELIMINATOR = "&";

    private Map<String, String> formMap;

    public FormBuilder() {
        formMap = new HashMap<>();
    }

    public FormBuilder(Map<String, String> initMap) {
        this.formMap = new HashMap<>(initMap);
    }

    public FormBuilder setParam(String key, String value)
    {
        formMap.put(key, value);
        return this;
    }

    public String build(){

        StringBuilder sb = new StringBuilder();
        formMap.forEach((key, value) -> sb.append(key).append("=").append(value).append(DELIMINATOR));

        return sb.toString();
    }
}
