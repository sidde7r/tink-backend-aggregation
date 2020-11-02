package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class ListSecuritiesResponse extends AbstractResponse {
    private BigDecimal marketValue;
    private String marketValueCurrency;
    private BigDecimal performance;
    private BigDecimal performancePct;
    private List<SecurityEntity> securities;
}
