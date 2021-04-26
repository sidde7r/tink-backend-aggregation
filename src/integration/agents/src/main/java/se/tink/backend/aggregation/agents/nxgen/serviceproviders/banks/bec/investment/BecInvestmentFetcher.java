package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.DepositAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.DepositDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BecInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BecApiClient apiClient;

    public BecInvestmentFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            FetchInvestmentResponse fetchInvestmentResponse = apiClient.fetchInvestment();
            return parseDeposits(fetchInvestmentResponse.getDepositAccounts())
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

    private Stream<InvestmentAccount> parseDeposits(
            List<DepositAccountEntity> depositAccountEntities) {
        return depositAccountEntities.stream()
                .map(
                        depositAccount -> {
                            List<Instrument> instruments = parseInstruments(depositAccount);

                            Portfolio portfolio = new Portfolio();
                            portfolio.setType(Portfolio.Type.DEPOT);
                            portfolio.setTotalValue(depositAccount.getMarketValue());
                            portfolio.setInstruments(instruments);
                            portfolio.setUniqueIdentifier(depositAccount.getId());

                            return depositAccount.toTinkInvestmentAccount(
                                    Collections.singletonList(portfolio));
                        });
    }

    private List<Instrument> parseInstruments(DepositAccountEntity depositAccount) {
        DepositDetailsResponse depositDetail =
                apiClient.fetchDepositDetail(depositAccount.getUrlDetail());

        Map<Boolean, List<PortfolioEntity>> partitionKnownInstrumentType =
                ListUtils.emptyIfNull(depositDetail.getPortfolios()).stream()
                        .collect(Collectors.partitioningBy(PortfolioEntity::isInstrumentTypeKnown));

        partitionKnownInstrumentType.get(false).forEach(this::logUnknownInstrumentType);

        return partitionKnownInstrumentType.get(true).stream()
                .flatMap(
                        portfolioEntity ->
                                ListUtils.emptyIfNull(portfolioEntity.getInstruments()).stream()
                                        .map(
                                                instrumentEntity ->
                                                        buildTinkInstrument(
                                                                portfolioEntity,
                                                                instrumentEntity,
                                                                depositAccount)))
                .collect(Collectors.toList());
    }

    private void logUnknownInstrumentType(PortfolioEntity portfolioEntity) {
        logger.info(
                "tag={} Unknown paper type[{}]: {}",
                BecConstants.Log.INVESTMENT_PAPER_TYPE,
                portfolioEntity.getDataType(),
                portfolioEntity.getInstrumentsType());
    }

    private Instrument buildTinkInstrument(
            PortfolioEntity portfolioEntity,
            InstrumentEntity instrumentEntity,
            DepositAccountEntity depositAccount) {

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(instrumentEntity.getId());
        instrument.setName(instrumentEntity.getPaperName());
        instrument.setQuantity(instrumentEntity.getNoOfPapers());
        instrument.setType(portfolioEntity.toTinkType());
        instrument.setRawType(portfolioEntity.getInstrumentsType());
        instrument.setPrice(instrumentEntity.getRate());
        instrument.setMarketValue(instrumentEntity.getRate() * instrumentEntity.getNoOfPapers());
        instrument.setCurrency(instrumentEntity.getCurrency());

        Optional<InstrumentDetailsEntity> instrumentDetailsEntity =
                tryGatheringDetails(instrumentEntity.getUrlDetail(), depositAccount.getAccountNo());

        instrumentDetailsEntity.ifPresent(
                details -> {
                    instrument.setIsin(details.getIsinCode());
                    instrument.setMarketPlace(details.getMarket());
                });

        return instrument;
    }

    private Optional<InstrumentDetailsEntity> tryGatheringDetails(
            String url, String accountNumber) {
        InstrumentDetailsEntity details = null;
        try {
            details = apiClient.fetchInstrumentDetails(url, accountNumber);
            logger.info("[BEC] successfully fetched investments details");
        } catch (HttpResponseException exception) {
            logger.warn("Fetching investment details failed.", exception);
        }
        return Optional.ofNullable(details);
    }
}
