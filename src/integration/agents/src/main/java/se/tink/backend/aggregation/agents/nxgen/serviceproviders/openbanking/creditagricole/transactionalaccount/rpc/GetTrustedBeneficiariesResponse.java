package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.BeneficiaryDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinksDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class GetTrustedBeneficiariesResponse {

    private List<BeneficiaryDto> beneficiaries;

    @JsonProperty("_links")
    private LinksDto links;
}
