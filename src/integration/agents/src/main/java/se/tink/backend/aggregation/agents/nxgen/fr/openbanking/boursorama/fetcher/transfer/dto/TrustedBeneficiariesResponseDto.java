package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.dto;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.BeneficiaryDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDtoBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TrustedBeneficiariesResponseDto implements TrustedBeneficiariesResponseDtoBase {

    private List<BeneficiaryDto> beneficiaries;

    @Override
    public LinksDtoBase getLinks() {
        return null;
    }
}
