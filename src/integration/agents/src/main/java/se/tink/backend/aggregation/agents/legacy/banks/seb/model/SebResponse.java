package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SebResponse {
    @JsonProperty("d")
    public Payload d = new Payload();

    @JsonProperty("X")
    public SystemStatus x = new SystemStatus();

    /**
     * Request is an error of the messages size are larger than 0. There can be several error
     * messages on a response.
     */
    public boolean hasErrors() {
        return d != null
                && d.getResultInfo() != null
                && d.getResultInfo().Messages != null
                && d.getResultInfo().Messages.size() > 0;
    }

    public List<ResultInfoMessage> getErrors() {
        if (hasErrors()) {
            return d.getResultInfo().Messages;
        } else {
            return ImmutableList.of();
        }
    }

    // Some errors might be missing an error text.s
    public Optional<ResultInfoMessage> getFirstErrorWithErrorText() {
        return getErrors().stream()
                .filter(
                        input ->
                                input != null
                                        && input.getErrorText() != null
                                        && !input.getErrorText().trim().isEmpty())
                .findFirst();
    }

    public Optional<String> getFirstErrorMessage() {
        return getFirstErrorWithErrorText().map(input -> input.getErrorText().trim());
    }

    public List<AccountEntity> getAccountEntities() {
        if (!isValid()) {
            return Collections.emptyList();
        }
        return d.getVodb().getAccountEntities();
    }

    public boolean isValid() {
        if (hasErrors()) {
            return false;
        }

        return !(Objects.isNull(d) || Objects.isNull(d.getVodb()));
    }

    public int getGatewayReturnCode() {
        return d.getResultInfo().gatewayReturnCode;
    }
}
