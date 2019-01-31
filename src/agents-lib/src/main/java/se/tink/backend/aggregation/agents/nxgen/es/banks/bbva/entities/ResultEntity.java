package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    private String code;
    private List<String> codes;
    private List<ErrorEntity> errors;
    private Integer severityLevel;

    @JsonIgnore
    public boolean hasError() {
        return !Optional.ofNullable(errors).orElse(Collections.emptyList()).isEmpty();
    }

    @JsonIgnore
    public boolean hasError(BbvaConstants.Error errorToFind) {
        return Optional.ofNullable(errors).orElse(Collections.emptyList()).stream()
                .anyMatch(e -> e.getError() == errorToFind);
    }

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
