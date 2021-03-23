package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@Builder
public class DecoupledAuthRequest {
    private String clientId;

    // Allows requesting a language for the text presented to the end user. This is a request for a
    // language to be used, the server may chose to not honor this request.This value is optional.
    // Providing an invalid value will not result in an error, instead the default language will be
    // used.If the value of this parameter is changed during an ongoing authorization attempt, the
    // change may not immediately be reflected in what is shown to the end user
    private String lang;

    @Builder.Default private String scope = QueryValues.SCOPE;

    // Bankid start mode, use AutoStartToken (ast) if bankid is to be launched on the same device
    // that the user is currently using, qr if the BankID is on another device
    @Builder.Default private String startMode = BodyValues.AST;
}
