package se.tink.backend.export.model.submodels;

import java.util.Map;
import java.util.stream.Collectors;

public class ExportCredential {

    private final String type;
    private final String providerName;
    private final String payload;   // Todo: check how to format these after deserialization
    private final String fields;    // Todo: check this too

    public ExportCredential(String type, String providerName, String payload, Map<String, String> fields) {
        this.type = type;
        this.providerName = providerName;
        this.payload = payload;
        this.fields = fieldsToString(fields);
    }

    public String getType() {
        return type;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getPayload() {
        return payload;
    }

    public String getFields() {
        return fields;
    }

    private String fieldsToString(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(" "));
    }
}
