package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.DepositAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.DepositDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(BecInvestmentFetcher.class);
    private final BecApiClient apiClient;

    public BecInvestmentFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            FetchInvestmentResponse fetchInvestmentResponse = apiClient.fetchInvestment();
            return Stream.concat(parseDeposits(fetchInvestmentResponse.getDepositAccounts()),
                    parseStocks(fetchInvestmentResponse.getStockOrders()))
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();
            if (httpResponse.getStatus() == HttpStatus.SC_FORBIDDEN) {
                // User does not have any investments
                return Collections.emptyList();
            }
            throw hre;
        }
    }

    private Stream<InvestmentAccount> parseDeposits(List<DepositAccountEntity> depositAccountEntities) {
        return depositAccountEntities.stream()
                .map(depositAccount -> {
                    List<Instrument> instruments = parseInstruments(depositAccount);

                    Portfolio portfolio = new Portfolio();
                    portfolio.setType(Portfolio.Type.DEPOT);
                    portfolio.setTotalValue(depositAccount.getMarketValue());
                    portfolio.setInstruments(instruments);
                    portfolio.setUniqueIdentifier(depositAccount.getId());

                    return depositAccount.toTinkInvestmentAccount(Collections.singletonList(portfolio));
                });
    }

    private Stream<InvestmentAccount> parseStocks(List<Object> stockOrders) {
        if (!stockOrders.isEmpty()) {
            log.infoExtraLong(SerializationUtils.serializeToString(stockOrders), BecConstants.Log.INVESTMENT_STOCKS);
        }
        return Stream.empty();
    }

    private List<Instrument> parseInstruments(DepositAccountEntity depositAccount) {
        List<Instrument> instruments = new ArrayList<>();

        DepositDetailsResponse depositDetail = apiClient.fetchDepositDetail(depositAccount.getUrlDetail());
        depositDetail.getPortfolios().stream()
                .filter(portfolioEntity -> {
                    if (!portfolioEntity.isInstrumentTypeKnown()) {
                        log.infoExtraLong(
                                String.format("Unknown paper type[%s]: %s, backend object: %s",
                                        portfolioEntity.getDataType(),
                                        portfolioEntity.getInstrumentsType(),
                                        SerializationUtils.serializeToString(portfolioEntity)),
                                BecConstants.Log.INVESTMENT_PAPER_TYPE);
                        return false;
                    }
                    return true;
                })
                .forEach(portfolioEntity ->
                    portfolioEntity.getInstruments().forEach(instrumentEntity -> {
                        InstrumentDetailsEntity instrumentDetailsEntity = apiClient.fetchInstrumentDetails(
                                instrumentEntity.getUrlDetail());

                        instruments.add(buildInstrument(portfolioEntity, instrumentEntity, instrumentDetailsEntity));
                    })
                );

        return instruments;
    }

    private Instrument buildInstrument(PortfolioEntity portfolioEntity, InstrumentEntity instrumentEntity,
            InstrumentDetailsEntity instrumentDetailsEntity) {
        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(instrumentDetailsEntity.getId());
        instrument.setName(instrumentEntity.getPaperName());
        instrument.setQuantity(instrumentEntity.getNoOfPapers());
        instrument.setType(portfolioEntity.toTinkType());
        instrument.setRawType(portfolioEntity.getInstrumentsType());
        instrument.setPrice(instrumentEntity.getRate());
        instrument.setMarketValue(instrumentEntity.getRate() * instrumentEntity.getNoOfPapers());
        instrument.setIsin(instrumentDetailsEntity.getIsinCode());
        instrument.setMarketPlace(instrumentDetailsEntity.getMarket());
        instrument.setCurrency(instrumentEntity.getCurrency());

        return instrument;
    }
}
