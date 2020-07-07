package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CodeExchangeReqResp {
    private String code;
}
