package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PortfolioEntity {
    private String currency;
    private String valueFundId;
    private String valueFund;
    private String shares;
    private String marketPrice;
    private String plusMinus;
    private String type;
    private String marketCode;
    private String isinCode;
    private BigDecimal marketValue;
}
