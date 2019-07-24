package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultInfo {

    @JsonProperty("Message")
    private List<ResultInfoMessage> messages;

    public List<ResultInfoMessage> getMessages() {
        return messages;
    }
}
