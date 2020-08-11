package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BalanceResourceDto {

    private AmountDto balanceAmount;

    private BalanceType balanceType;

    private String name;
}
