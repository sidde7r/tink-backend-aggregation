package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioContentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioRepositionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
@XmlRootElement(name = "methodResult")
public class PortfolioDetailsResponse {
    @JsonIgnore
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InfoEntity info;
    @JsonProperty("finLista")
    private String endOfList;
    @JsonProperty("posicionContratoValores")
    private AmountEntity totalMarketValue;
    @JsonProperty("repos")
    private PortfolioRepositionEntity paginationData;
    @JsonProperty("lista")
    private List<PortfolioContentEntity> portfolioContents;

    @JsonIgnore
    public InvestmentAccount toTinkInvestment(SantanderEsApiClient apiClient, String userDataXml,
            PortfolioEntity portfolio, List<PortfolioContentEntity> portfolioContent, HolderName holderName) {
        List<Instrument> instruments = getInstruments(apiClient, userDataXml, portfolioContent);
        List<Portfolio> portfolios = toTinkPortfolio(portfolio, instruments);

        return InvestmentAccount.builder(portfolio.getContractId().getAccountNumber())
                .setCashBalance(Amount.inEUR(0.0))
                .setName(portfolio.getGeneralInfo().getAlias())
                .setAccountNumber(portfolio.getContractId().getAccountNumber())
                .setHolderName(holderName)
                .setPortfolios(portfolios)
                .build();
    }

    @JsonIgnore
    private List<Instrument> getInstruments(SantanderEsApiClient apiClient, String userDataXml,
            List<PortfolioContentEntity> portfolioContent) {
        return Optional.ofNullable(portfolioContent).orElse(Collections.emptyList()).stream()
                .map(item -> item.toInstrument(apiClient, userDataXml))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private List<Portfolio> toTinkPortfolio(PortfolioEntity portfolio, List<Instrument> instruments) {
        Portfolio tinkPortfolio = new Portfolio();
        tinkPortfolio.setUniqueIdentifier(portfolio.getContractId().getAccountNumber());
        tinkPortfolio.setInstruments(instruments);
        tinkPortfolio.setTotalValue(portfolio.getTotalValue().getTinkAmount().doubleValue());
        tinkPortfolio.setType(Portfolio.Type.DEPOT);
        tinkPortfolio.setCashValue(0.0);

        return Collections.singletonList(tinkPortfolio);
    }

    @JsonIgnore
    public boolean moreData() {
        return SantanderEsConstants.Indicators.NO.equalsIgnoreCase(endOfList);
    }

    public List<PortfolioContentEntity> getPortfolioContents() {
        return portfolioContents;
    }

    public PortfolioRepositionEntity getPaginationData() {
        return paginationData;
    }
}
