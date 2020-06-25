package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.CreditorDtoBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class CreditorDto implements CreditorDtoBase {

    private String name;

    private PostalAddressDto postalAddress;
}
