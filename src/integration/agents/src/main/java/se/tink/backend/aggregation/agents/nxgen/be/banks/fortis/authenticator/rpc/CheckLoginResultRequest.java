package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.UcrEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@JsonInclude(Include.NON_NULL)
public class CheckLoginResultRequest {
    private final UcrEntity ucr;
    private final EasyPinEntity easyPin;
    private final String distributorId;
    private final String smid;
}
