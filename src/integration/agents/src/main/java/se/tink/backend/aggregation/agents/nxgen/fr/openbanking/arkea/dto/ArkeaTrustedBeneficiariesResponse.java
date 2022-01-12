package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaTrustedBeneficiariesLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaTrustedBeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDtoBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaTrustedBeneficiariesResponse implements TrustedBeneficiariesResponseDtoBase {

    private List<ArkeaTrustedBeneficiaryEntity> beneficiaries;

    @JsonProperty("_links")
    private ArkeaTrustedBeneficiariesLinksEntity trustedBeneficiariesLinksEntity;

    @Override
    public LinksDtoBase getLinks() {
        return trustedBeneficiariesLinksEntity;
    }
}
