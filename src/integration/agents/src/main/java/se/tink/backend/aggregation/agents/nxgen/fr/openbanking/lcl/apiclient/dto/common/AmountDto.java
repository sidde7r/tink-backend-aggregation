package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AmountDto {

    private String amount;

    private String currency;
}
