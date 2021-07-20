package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PaymentAuthorizationResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String psuId;

    private List<ScaMethodEntity> scaMethods;

    private String transactionStatus;
}
