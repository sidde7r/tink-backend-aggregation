package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class SubPlacementEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(SubPlacementEntity.class);

    private String type;
    private String name;
    private List<DetailedHoldingEntity> holdings;
    private AmountEntity aggregatedMarketValue;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<DetailedHoldingEntity> getHoldings() {
        return holdings;
    }

    public AmountEntity getAggregatedMarketValue() {
        return aggregatedMarketValue;
    }

    public List<Instrument> toTinkFundInstruments(SwedbankSEApiClient apiClient) {
        List<Instrument> instruments = new ArrayList<>();
        for (DetailedHoldingEntity holding : getHoldingsOrEmptyList()) {
            LinksEntity links = holding.getLinks();
            if (links == null || links.getNext() == null) {
                continue;
            }

            FundMarketInfoResponse fundMarketInfoResponse =
                    apiClient.fundMarketInfo(links.getNext());

            holding.toTinkFundInstrument(fundMarketInfoResponse.getIsincode())
                    .ifPresent(instruments::add);
        }

        return instruments;
    }

    public List<Instrument> toTinkInstruments(SwedbankSEApiClient apiClient) {
        List<Instrument> instruments = new ArrayList<>();
        for (DetailedHoldingEntity holding : getHoldingsOrEmptyList()) {
            if (Strings.isNullOrEmpty(this.type)) {
                continue;
            }

            switch (this.type.toLowerCase()) {
                case "equity":
                case "equities":
                case "subscription_right":
                case "spax":
                case "etf":
                case "warrant":
                case "fixed_income":
                case "interestequity":
                    holding.toTinkInstrument(this.type).ifPresent(instruments::add);
                    break;
                case "funds":
                case "equityfund":
                case "mixedfund":
                case "interestfund":
                case "alternativefund":
                    String isinCode = getIsinOfFund(apiClient, holding.getFundCode());
                    holding.toTinkFundInstrument(isinCode).ifPresent(instruments::add);
                    break;
                default:
                    log.warn("Previously unknown subplacement type:[{}]", type);
            }
        }

        return instruments;
    }

    /**
     * We are passing the apiClient all the way down to this entity. The architecture of this flow
     * should be reworked.
     */
    public List<InstrumentModule> toTinkPensionInstruments(SwedbankSEApiClient apiClient) {
        List<InstrumentModule> instrumentModules = new ArrayList<>();
        for (DetailedHoldingEntity holding : getHoldingsOrEmptyList()) {
            String isinCode = getIsinOfFund(apiClient, holding.getFundCode());
            instrumentModules.add(holding.toTinkInstrumentModule(isinCode));
        }
        return instrumentModules;
    }

    private String getIsinOfFund(SwedbankSEApiClient apiClient, String fundCode) {
        if (fundCode == null) {
            return null;
        }

        FundMarketInfoResponse fundMarketInfoResponse = apiClient.fundMarketInfo(fundCode);
        return fundMarketInfoResponse.getIsincode();
    }

    @JsonIgnore
    private List<DetailedHoldingEntity> getHoldingsOrEmptyList() {
        return Optional.ofNullable(holdings).orElseGet(Collections::emptyList);
    }
}
