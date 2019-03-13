package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import io.vavr.control.Option;
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
        return !Option.of(errors).getOrElse(List.empty()).isEmpty();
    }

    @JsonIgnore
    public boolean hasError(BbvaConstants.Error errorToFind) {
        return !Option.of(errors).getOrElse(List.empty())
                .filter(e -> e.getError() == errorToFind)
                .isEmpty();
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
