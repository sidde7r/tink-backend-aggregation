package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.BOND;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.CERTIFICATE;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.EQUITY_LINKED_BOND;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.EXCHANGE_TRADED_FUND;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.FUND;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.FUTURE_FORWARD;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.STOCK;
import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes.WARRANT;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.BondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.CertificateMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.EquityLinkedBondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.ExchangeTradedFundInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FutureForwardMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.MarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.StockMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.WarrantMarketInfoResponse;

@RunWith(JUnitParamsRunner.class)
public class AvanzaApiClientTest {

    private AvanzaApiClient avanzaApiClient;

    @Before
    public void setup() {
        this.avanzaApiClient = mock(AvanzaApiClient.class);
    }

    @Test
    @Parameters(method = "getParameters")
    @SuppressWarnings("unchecked")
    public void shouldReturnFundMarketInfoResponseFromFundInstrumentType(
            String instrumentType, Class className) throws ClassNotFoundException {

        // given
        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();

        // when
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(instrumentType), anyString(), anyString(), eq(className)))
                .thenReturn(mock(className));

        // then
        Class<MarketInfoResponse> result =
                (Class<MarketInfoResponse>)
                        avanzaApiClient
                                .getInstrumentMarketInfo(
                                        instrumentType, "dummyString", "anotherDummyString")
                                .getClass();

        assertTrue(
                result.getTypeName()
                        .contains(((Class<MarketInfoResponse>) className).getSimpleName()));
    }

    private Object[] getParameters() {
        return new Object[] {
            new Object[] {FUND, FundMarketInfoResponse.class},
            new Object[] {STOCK, StockMarketInfoResponse.class},
            new Object[] {CERTIFICATE, CertificateMarketInfoResponse.class},
            new Object[] {FUTURE_FORWARD, FutureForwardMarketInfoResponse.class},
            new Object[] {EQUITY_LINKED_BOND, EquityLinkedBondMarketInfoResponse.class},
            new Object[] {BOND, BondMarketInfoResponse.class},
            new Object[] {WARRANT, WarrantMarketInfoResponse.class},
            new Object[] {EXCHANGE_TRADED_FUND, ExchangeTradedFundInfoResponse.class}
        };
    }

    @Test
    public void shouldReturnNullFromNullValuedInstrumentType() {
        assertNull(
                avanzaApiClient.getInstrumentMarketInfo(null, "dummyString", "anotherDummyString"));
    }
}
