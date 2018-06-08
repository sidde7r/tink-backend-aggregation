package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    private String code;
    private List<String> codes;
    private Integer severityLevel;

    public String getCode() {
        return code;
    }

    public List<String> getCodes() {
        return codes;
    }

    public Integer getSeverityLevel() {
        return severityLevel;
    }
}
