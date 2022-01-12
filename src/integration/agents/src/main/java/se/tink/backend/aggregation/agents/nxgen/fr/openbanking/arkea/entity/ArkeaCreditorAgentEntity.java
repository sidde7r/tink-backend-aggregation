package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.CreditorAgentDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaCreditorAgentEntity implements CreditorAgentDtoBase {

    private String bicFi;
}
