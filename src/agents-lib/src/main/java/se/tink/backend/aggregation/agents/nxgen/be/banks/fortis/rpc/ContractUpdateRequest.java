package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.SignatureEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractUpdateRequest {
    private SignatureEntity signature;
    private String tcFlag;
    private String password;
}
