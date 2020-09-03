package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class BeneficiaryItemDto {

    private IdentificationDto identification;

    private TransferCreditorIdentityDto transferCreditorIdentity;
}
