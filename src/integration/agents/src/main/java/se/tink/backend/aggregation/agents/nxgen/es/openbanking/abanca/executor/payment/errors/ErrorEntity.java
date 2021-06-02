package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.errors;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ErrorEntity {
    private String id;
    private String code;
    private String title;
    private String details;
    private String meta;
}
