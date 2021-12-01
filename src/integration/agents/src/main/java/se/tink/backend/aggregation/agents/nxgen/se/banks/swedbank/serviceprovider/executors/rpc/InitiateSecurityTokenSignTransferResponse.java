package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSecurityTokenSignTransferResponse {
    private String description;
    @Getter private String challenge;
    private List<String> highlights;
    private String annotatedChallenge;
    private String annotatedDescription;
    @Getter private LinksEntity links;
}
