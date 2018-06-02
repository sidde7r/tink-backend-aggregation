package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.utils.IsNot0Matcher.isNot0;

public class HandelsbankenSEInvestmentFetcherTest extends HandelsbankenSEAuthenticatedTest {
    @Test
    public void fetchingInvestmentData() throws Exception {
        autoAuthenticator.autoAuthenticate();
        HandelsbankenSEInvestmentFetcher fetcher = new HandelsbankenSEInvestmentFetcher(client, sessionStorage, credentials);

        Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();
        assertThat(accounts, notNullValue());
        assertFalse(accounts.isEmpty());

        accounts.forEach(account -> {
            assertAccountValues(account);
            assertPortfolioValues(account.getPortfolios());
        });
    }

    private void assertAccountValues(InvestmentAccount account) {
        assertThat(account, notNullValue());
        assertThat(account.getType(), is(AccountTypes.INVESTMENT));
        assertThat(account.getBalance().getValue(), isNot0());
        assertThat(account.getName(), notNullValue());
        assertThat(account.getAccountNumber(), notNullValue());
        assertThat(account.getBankIdentifier(), notNullValue());
    }

    private void assertPortfolioValues(List<Portfolio> portfolios) {
        assertThat(portfolios, notNullValue());
        assertFalse(portfolios.isEmpty());
        portfolios.forEach(portfolio -> {
            assertThat(portfolio.getRawType(), notNullValue());
            assertThat(portfolio.getType(), notNullValue());
            assertThat(portfolio.getTotalValue(), notNullValue());
            assertThat(portfolio.getTotalValue(), isNot0());
            assertThat(portfolio.getTotalProfit(), notNullValue());
            assertThat(portfolio.getTotalProfit(), isNot0());
            assertThat(portfolio.getUniqueIdentifier(), notNullValue());

            assertInstruments(portfolio);
        });
    }

    private void assertInstruments(Portfolio portfolio) {
        List<Instrument> instruments = portfolio.getInstruments();
        assertThat(instruments, notNullValue());
        assertFalse(instruments.isEmpty());
        instruments.forEach(instrument -> {
            assertThat(instrument.getType(), is(Instrument.Type.FUND));
            assertThat(instrument.getAverageAcquisitionPrice(), notNullValue());
            assertThat(instrument.getAverageAcquisitionPrice(), isNot0());
            assertThat(instrument.getCurrency(), notNullValue());
            assertThat(instrument.getIsin(), notNullValue());
            assertThat(instrument.getMarketValue(), notNullValue());
            assertThat(instrument.getMarketValue(), isNot0());
            assertThat(instrument.getName(), notNullValue());
            assertThat(instrument.getPrice(), notNullValue());
            assertThat(instrument.getPrice(), isNot0());
            assertThat(instrument.getProfit(), notNullValue());
            assertThat(instrument.getProfit(), isNot0());
            assertThat(instrument.getQuantity(), notNullValue());
            assertThat(instrument.getQuantity(), isNot0());
            assertThat(instrument.getRawType(), notNullValue());
            assertThat(instrument.getUniqueIdentifier(), notNullValue());
        });
    }

}
