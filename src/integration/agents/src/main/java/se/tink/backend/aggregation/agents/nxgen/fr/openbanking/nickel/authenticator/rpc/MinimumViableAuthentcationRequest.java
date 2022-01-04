package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.entity.NickelClientData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@Data
public class MinimumViableAuthentcationRequest {

    @JsonProperty("barcode")
    private String userId;

    private String password;

    private NickelClientData client;

    @JsonInclude(Include.NON_NULL)
    private String mfaToken;
}
