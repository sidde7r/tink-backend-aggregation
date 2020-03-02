package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountsResponseDto {

    private IdentifiersDto identifiers;
    private HolderDto holder;
    private ProductDto product;
    private StatusDto status;
}
