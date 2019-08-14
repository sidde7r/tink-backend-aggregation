package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultInfo {
    @JsonProperty("Message")
    private List<ResultInfoMessage> messages;

    @JsonIgnore
    public List<ResultInfoMessage> getMessages() {
        return messages;
    }
}
