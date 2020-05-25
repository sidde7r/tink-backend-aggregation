package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TrustedBeneficiariesResponse {

    private List<BeneficiaryDto> beneficiaries;

    @JsonProperty("_links")
    private LinksDto links;
}
