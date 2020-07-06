package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Parameters;

@Getter
public class OAuth2V2AuthorizeResponse {

    @JsonProperty("enctype")
    private String enctype;

    @JsonProperty("method")
    private String method;

    @JsonProperty("action")
    private String action;

    @JsonProperty("parameters")
    private Parameters parameters;

    @JsonIgnore
    public boolean isValid() {
        return !Strings.isNullOrEmpty(action)
                && !Strings.isNullOrEmpty(method)
                && Objects.nonNull(parameters)
                && !Strings.isNullOrEmpty(parameters.getSAMLRequest());
    }

    public String getSAMLRequest() {
        if (!isValid()) {
            return "";
        }
        return parameters.getSAMLRequest();
    }
}
