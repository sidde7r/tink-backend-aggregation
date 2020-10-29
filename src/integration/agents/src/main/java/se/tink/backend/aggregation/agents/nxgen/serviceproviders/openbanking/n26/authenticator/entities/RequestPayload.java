package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities.TransferBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@NoArgsConstructor
public class RequestPayload {

    private String callbackState;
    private ToEntity to;
    private AccessBodyEntity accessBody;
    private TransferBodyEntity transferBody;
    private String redirectUrl;
    private String refId;

    public RequestPayload(
            String callbackState,
            ToEntity to,
            AccessBodyEntity accessBody,
            String redirectUrl,
            String refId) {
        this.callbackState = callbackState;
        this.to = to;
        this.accessBody = accessBody;
        this.redirectUrl = redirectUrl;
        this.refId = refId;
    }

    public RequestPayload(
            String callbackState,
            ToEntity to,
            TransferBodyEntity transferBody,
            String redirectUrl) {
        this.callbackState = callbackState;
        this.to = to;
        this.transferBody = transferBody;
        this.redirectUrl = redirectUrl;
    }
}
