package se.tink.backend.aggregation.agents.tools.opsgenie.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@JsonObject
public class CreateAlertRequest {
    private String message;
    private String description;
    private List<String> actions = Collections.singletonList("Create");
    private List<String> tags;
    private String entity;
    private String priority;
}
