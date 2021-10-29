package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.CreditorDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaCreditorEntity implements CreditorDtoBase {

    private String name;
    private ArkeaPostalAddressEntity postalAddress;
}
