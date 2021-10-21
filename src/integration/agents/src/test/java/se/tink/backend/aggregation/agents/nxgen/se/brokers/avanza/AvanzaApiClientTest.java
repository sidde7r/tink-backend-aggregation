package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.BondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.CertificateMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.EquityLinkedBondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.ExchangeTradedFundInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FutureForwardMarketInfoResponse;
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
    public void shouldReturnFundMarketInfoResponseFromFundInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.FUND),
                        anyString(),
                        anyString(),
                        eq(FundMarketInfoResponse.class)))
                .thenReturn(mock(FundMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.FUND, "dummyString", "anotherDummyString")
                        instanceof FundMarketInfoResponse);
    }

    @Test
    public void shouldReturnStockMarketInfoResponseFromStockInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.STOCK),
                        anyString(),
                        anyString(),
                        eq(StockMarketInfoResponse.class)))
                .thenReturn(mock(StockMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.STOCK, "dummyString", "anotherDummyString")
                        instanceof StockMarketInfoResponse);
    }

    @Test
    public void shouldReturnCertificateMarketInfoResponseFromCertificateInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.CERTIFICATE),
                        anyString(),
                        anyString(),
                        eq(CertificateMarketInfoResponse.class)))
                .thenReturn(mock(CertificateMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.CERTIFICATE, "dummyString", "anotherDummyString")
                        instanceof CertificateMarketInfoResponse);
    }

    @Test
    public void shouldReturnFutureForwardMarketInfoResponseFromFutureForwardInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.FUTURE_FORWARD),
                        anyString(),
                        anyString(),
                        eq(FutureForwardMarketInfoResponse.class)))
                .thenReturn(mock(FutureForwardMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.FUTURE_FORWARD, "dummyString", "anotherDummyString")
                        instanceof FutureForwardMarketInfoResponse);
    }

    @Test
    public void shouldReturnEquityLinkedBondMarketInfoResponseFromEquityLinkedBondInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.EQUITY_LINKED_BOND),
                        anyString(),
                        anyString(),
                        eq(EquityLinkedBondMarketInfoResponse.class)))
                .thenReturn(mock(EquityLinkedBondMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.EQUITY_LINKED_BOND,
                                "dummyString",
                                "anotherDummyString")
                        instanceof EquityLinkedBondMarketInfoResponse);
    }

    @Test
    public void shouldReturnBondMarketInfoResponseFromBondInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.BOND),
                        anyString(),
                        anyString(),
                        eq(BondMarketInfoResponse.class)))
                .thenReturn(mock(BondMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.BOND, "dummyString", "anotherDummyString")
                        instanceof BondMarketInfoResponse);
    }

    @Test
    public void shouldReturnWarrantMarketInfoResponseFromWarrantInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.WARRANT),
                        anyString(),
                        anyString(),
                        eq(WarrantMarketInfoResponse.class)))
                .thenReturn(mock(WarrantMarketInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.WARRANT, "dummyString", "anotherDummyString")
                        instanceof WarrantMarketInfoResponse);
    }

    @Test
    public void
            shouldReturnExchangeTradedFundMarketInfoResponseFromExchangeTradedFundInstrumentType() {

        when(avanzaApiClient.getInstrumentMarketInfo(anyString(), anyString(), anyString()))
                .thenCallRealMethod();
        when(avanzaApiClient.fetchMarketInfoResponse(
                        eq(InstrumentTypes.EXCHANGE_TRADED_FUND),
                        anyString(),
                        anyString(),
                        eq(ExchangeTradedFundInfoResponse.class)))
                .thenReturn(mock(ExchangeTradedFundInfoResponse.class));

        assertTrue(
                avanzaApiClient.getInstrumentMarketInfo(
                                InstrumentTypes.EXCHANGE_TRADED_FUND,
                                "dummyString",
                                "anotherDummyString")
                        instanceof ExchangeTradedFundInfoResponse);
    }

    @Test
    public void shouldReturnNullFromNullValuedInstrumentType() {

        assertEquals(
                avanzaApiClient.getInstrumentMarketInfo(null, "dummyString", "anotherDummyString"),
                null);
    }
}
