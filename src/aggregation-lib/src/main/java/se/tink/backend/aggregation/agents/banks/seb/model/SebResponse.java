package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SebResponse {
    @JsonProperty("d")
    public Payload d = new Payload();
    @JsonProperty("X")
    public SystemStatus x = new SystemStatus();

    /**
     * Request is an error of the messages size are larger than 0. There can be several error messages on a
     * response.
     */
    public boolean hasErrors() {
        return d != null && d.resultInfo != null && d.resultInfo.Messages != null && d.resultInfo.Messages.size() > 0;
    }

    public List<ResultInfoMessage> getErrors() {
        if (hasErrors()) {
            return d.resultInfo.Messages;
        } else {
            return ImmutableList.of();
        }
    }

    // Some errors might be missing an error text.s
    public Optional<ResultInfoMessage> getFirstErrorWithErrorText() {
        return getErrors().stream()
                .filter(input -> input != null && input.ErrorText != null && !input.ErrorText.trim().isEmpty())
                .findFirst();
    }

    public Optional<String> getFirstErrorMessage() {
        return getFirstErrorWithErrorText().map(input -> input.ErrorText.trim());
    }

}
