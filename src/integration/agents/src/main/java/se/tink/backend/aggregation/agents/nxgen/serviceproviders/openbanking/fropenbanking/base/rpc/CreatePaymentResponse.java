package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;
}
