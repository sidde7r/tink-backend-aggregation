package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;

@JsonObject
public class PortfolioEntity {
    @JsonProperty private Boolean onlyDefaultAccountForTransactions;
    @JsonProperty private String ownerName = "";
    @JsonProperty private String paymentReference = "";
    @JsonProperty private Boolean plegded;
    @JsonProperty private String portfolioId = "";
    @JsonProperty private String portfolioName = "";
    @JsonProperty private List<PositionsEntity> positions;
    @JsonProperty private BigDecimal totalMarketValue;
    @JsonProperty private String type = "";

    @JsonIgnore
    public String getPortfolioId() {
        return portfolioId;
    }

    @JsonIgnore
    public String getPortfolioName() {
        return portfolioName;
    }

    @JsonIgnore
    public String getOwnerName() {
        return ownerName;
    }

    @JsonIgnore
    public List<PositionsEntity> getPositions() {
        return Optional.ofNullable(positions).orElse(Collections.emptyList());
    }

    @JsonIgnore
    public PortfolioModule toTinkPortfolio(List<InstrumentModule> instruments) {
        return PortfolioModule.builder()
                .withType(getType())
                .withUniqueIdentifier(portfolioId)
                .withCashValue(0)
                .withTotalProfit(calculateTotalProfit().doubleValue())
                .withTotalValue(totalMarketValue.doubleValue())
                .withInstruments(instruments)
                .setRawType(type)
                .build();
    }

    @JsonIgnore
    private PortfolioType getType() {
        return SpankkiConstants.PORTFOLIO_TYPE_MAP.translate(type).orElse(PortfolioType.OTHER);
    }

    @JsonIgnore
    private BigDecimal calculateTotalProfit() {
        if (BigDecimal.ZERO.equals(totalMarketValue.setScale(0, RoundingMode.HALF_UP))) {
            return BigDecimal.ZERO;
        }
        return positions.stream()
                .map(PositionsEntity::calculateOriginalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(totalMarketValue, 3, RoundingMode.HALF_UP);
    }
}
