package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenAccount;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AlandsBankenInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(AlandsBankenInvestmentsFetcher.class);

    private final AlandsBankenApiClient client;

    public AlandsBankenInvestmentsFetcher(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts().stream()
                .filter(AlandsBankenAccount::isInvestmentAccount)
                .map(account -> {
                    if (!account.isKnownPortfolioType()) {
                        logPortfolioData(account);
                    }
                    return account.toInvestmentAccount(fetchPortfolio(account));
                })
                .collect(Collectors.toList());
    }

    private Portfolio fetchPortfolio(AlandsBankenAccount account) {
        PortfolioResponse portfolioResponse = client.fetchPortfolio(account.getAccountId());
        Portfolio portfolio = portfolioResponse.toTinkPortfolio(account);
        portfolio.setInstruments(fetchInstruments(portfolioResponse));
        return portfolio;
    }

    private List<Instrument> fetchInstruments(PortfolioResponse portfolioResponse) {
        return portfolioResponse.getInstrumentGroups().stream()
                .flatMap(instrumentGroup -> instrumentGroup.getInstruments().stream()
                        .filter(InstrumentEntity::hasIsinCode)
                        .map(instrument -> toTinkInstrument(instrumentGroup.getTypeOfInstruments(), instrument))
                )
                .collect(Collectors.toList());
    }

    private Instrument toTinkInstrument(int typeOfInstrument, InstrumentEntity instrument) {
        switch (typeOfInstrument) {
        case 0: // TODO verify if for type 0, we can fecth intrument Details
        case 1:
            return instrument.toTinkInstrument(typeOfInstrument, fetchInstrumentDetails(typeOfInstrument, instrument));
        case 2:
            return instrument.toTinkInstrument(typeOfInstrument,
                    isFund(typeOfInstrument, instrument) ? Instrument.Type.FUND : Instrument.Type.OTHER);
        default:
            logInstrumentData(typeOfInstrument, instrument);
            return instrument.toTinkInstrument(typeOfInstrument);
        }
    }

    private boolean isFund(int typeOfInstrument, InstrumentEntity instrument) {
        boolean isFund = client.fetchFundInfo(instrument.getInstrumentId()).getStatus().isSuccess();
        if (!isFund) {
            logInstrumentData(typeOfInstrument, instrument);
        }
        return isFund;
    }

    private InstrumentDetailsEntity fetchInstrumentDetails(int typeOfInstrument, InstrumentEntity instrument) {
        InstrumentDetailsEntity instrumentDetails = client.fetchInstrumentDetails(
                instrument.getIsinCode(), instrument.getMarketPlace()).getInstrumentDetails();
        if (instrumentDetails.getInstrumentGroup() != null) {
            if (!instrumentDetails.isKnownType()) {
                logInstrumentData(typeOfInstrument, instrument, instrumentDetails);
            }
        }
        return instrumentDetails;
    }

    private void logPortfolioData(AlandsBankenAccount account) {
        log.infoExtraLong(String.format("account: %s portfolio: %s",
                SerializationUtils.serializeToString(account),
                client.fetchPortfolioAsString(account.getAccountId())),
                AlandsBankenConstants.Fetcher.INVESTMENT_PORTFOLIO_LOGGING);
    }

    private void logInstrumentData(int typeOfInstrument, InstrumentEntity instrument) {
        log.infoExtraLong(String.format("typeOfInstrument: %s instrument: %s",
                typeOfInstrument,
                SerializationUtils.serializeToString(instrument)),
                AlandsBankenConstants.Fetcher.INVESTMENT_INSTRUMENT_LOGGING);
    }

    private void logInstrumentData(int typeOfInstrument, InstrumentEntity instrument,
            InstrumentDetailsEntity instrumentDetails) {
        log.infoExtraLong(String.format("typeOfInstrument: %s instrument: %s instrumentDetails: %s",
                typeOfInstrument,
                SerializationUtils.serializeToString(instrument),
                SerializationUtils.serializeToString(instrumentDetails)),
                AlandsBankenConstants.Fetcher.INVESTMENT_INSTRUMENT_LOGGING);
    }
}
