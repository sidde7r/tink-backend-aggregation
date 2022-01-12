package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.CreditorAccountDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaCreditorAccountEntity implements CreditorAccountDtoBase {

    private String iban;
}
