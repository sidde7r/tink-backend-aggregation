package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TaxChargesDto {

    private AmountDto amount;

    private String identification;

    private String rate;
}
