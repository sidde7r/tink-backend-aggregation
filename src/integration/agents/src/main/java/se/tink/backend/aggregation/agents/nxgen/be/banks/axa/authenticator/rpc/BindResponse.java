package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AnonymousInvokeBindResponseData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BindResponse extends BaseResponse<AnonymousInvokeBindResponseData> {}
