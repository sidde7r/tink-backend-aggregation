package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPayload {

    private String callbackState;
    private ToEntity to;
    private AccessBodyEntity accessBody;
    private String redirectUrl;
}
