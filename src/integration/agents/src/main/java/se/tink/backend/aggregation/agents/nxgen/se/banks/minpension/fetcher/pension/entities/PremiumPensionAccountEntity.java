package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;

@Getter
@JsonObject
public class PremiumPensionAccountEntity {
    @JsonProperty("ejPlacerbartKapital")
    private Double nonPositionableCapital;

    @JsonProperty("fondhandelPagar")
    private boolean fundTradingOngoing;

    @JsonProperty("forsakringsform")
    private String insuranceType;

    @JsonProperty("kapital")
    private Double capital;

    @JsonProperty("typ")
    private String type;

    @JsonProperty("manadsbeloppVidFulltUttag")
    private Double monthlyAmountAtFullWithdrawal;

    @JsonProperty("fondList")
    private List<FundListEntity> fundList;

    PortfolioModule getPremiumPension(String ssn) {
        return PortfolioModule.builder()
                .withType(PortfolioType.PENSION)
                .withUniqueIdentifier(getIdentifier(ssn))
                .withCashValue(0.00)
                .withTotalProfit(capital)
                .withTotalValue(capital)
                .withInstruments(toTinkInstrument())
                .build();
    }

    private String getIdentifier(String ssn) {
        return AccountTypes.PREMIUM_PENSION + "-" + type + "-" + insuranceType + "-" + ssn;
    }

    private List<InstrumentModule> toTinkInstrument() {
        return ListUtils.emptyIfNull(fundList).stream()
                .map(FundListEntity::toTinkInstrument)
                .collect(Collectors.toList());
    }
}
