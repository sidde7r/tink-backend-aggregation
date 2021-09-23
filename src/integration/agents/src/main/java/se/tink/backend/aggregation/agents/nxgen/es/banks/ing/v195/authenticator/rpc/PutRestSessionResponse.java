package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PutRestSessionResponse {

    private String ticket;

    private Long timeoutInSeconds;

    private String rememberMeToken;

    private Integer resultCode;

    private String resultMessage;

    private Integer nextValMethod;

    private String personId;
}
