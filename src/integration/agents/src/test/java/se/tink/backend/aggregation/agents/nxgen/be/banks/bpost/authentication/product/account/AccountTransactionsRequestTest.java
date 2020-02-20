package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.math.BigDecimal;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.WireMockIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class AccountTransactionsRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContext;

    @Before
    public void init() {
        authContext = Mockito.mock(BPostBankAuthContext.class);
    }

    @Test
    public void withBodyShouldCreateExpectedRequestBody() throws JSONException {
        // given
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        AccountTransactionsRequest objectUnderTest =
                new AccountTransactionsRequest(authContext, 0, 21, "BE68539007547034");
        ArgumentCaptor<String> stringArgumentCapture = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCapture.capture());
        JSONObject body = new JSONObject(stringArgumentCapture.getValue());
        Assert.assertEquals("0", body.getString("f"));
        Assert.assertEquals("21", body.getString("l"));
        Assert.assertEquals("539007547034", body.getString("accountNumber"));
    }

    @Test
    public void executeShouldReturnTransactionList() throws RequestException {
        // given
        final String responseBody =
                "[{\"bookingDateTime\": \"2019-12-11T00:00:00+0000\",\"standardWording\": \"ZPREITTF\",\"categoryId\": \"Ontv. order klant-trsf\",\"transactionAmount\": 50,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"51238201814\",\"counterpartyName\": \"TINK AB\",\"accountingReference\": \"B9L11REWUO57LMDV\",\"creditDebitIndicator\": \"CRDT\",\"operationReference\": \"B9L11REWUO57LMDV\",\"sortField\": \"0000003\",\"identifier\": \"1234567890123456789012345678901234567890\"}, {\"bookingDateTime\": \"2019-10-15T00:00:00+0000\",\"standardWording\": \"PGCTSTDB\",\"categoryId\": \"Uw overschrijving\",\"transactionAmount\": -1,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE97377100580549\",\"counterpartyName\": \"Wouter\",\"accountingReference\": \"B9J15PGRSP92Q4NS\",\"creditDebitIndicator\": \"DBIT\",\"operationReference\": \"B9J15PGRSP92Q4NS\",\"sortField\": \"0000002\",\"identifier\": \"1234567890123456789012345678901234567890\"}, {\"bookingDateTime\": \"2019-10-15T00:00:00+0000\",\"standardWording\": \"PGCTSTCR\",\"categoryId\": \"Overschrijving in uw voordeel\",\"transactionAmount\": 5,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE03750687072784\",\"counterpartyName\": \"Brysbaert Stijn\",\"accountingReference\": \"B9J15XM01V009753\",\"creditDebitIndicator\": \"CRDT\",\"operationReference\": \"B9J15XM01V009753\",\"sortField\": \"0000001\",\"identifier\": \"20191015µB9J15XM01V009753µBE92000450043523µ0000001\"}, {\"bookingDateTime\": \"2018-12-13T00:00:00+0000\",\"standardWording\": \"PGCTSTDB\",\"categoryId\": \"Uw overschrijving\",\"transactionAmount\": -5,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE74143105352007\",\"counterpartyName\": \"Wouter V\",\"accountingReference\": \"B8L13CWTEY6K0P2L\",\"creditDebitIndicator\": \"DBIT\",\"operationReference\": \"B8L13CWTEY6K0P2L\",\"sortField\": \"0000005\",\"identifier\": \"20181213µB8L13CWTEY6K0P2LµBE92000450043523µ0000005\"}, {\"bookingDateTime\": \"2018-08-20T00:00:00+0000\",\"standardWording\": \"NCCADBCM\",\"categoryId\": \"Maestro-betaling\",\"transactionAmount\": -1.94,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE54000000000000\",\"counterpartyName\": \"7-ELEVEN 4217107\",\"accountingReference\": \"B8H20NC0030002BZ\",\"creditDebitIndicator\": \"DBIT\",\"operationReference\": \"B8H20NC0030002BZ\",\"sortField\": \"0000004\",\"identifier\": \"20180820µB8H20NC0030002BZµBE92000450043523µ0000004\"}, {\"bookingDateTime\": \"2018-08-20T00:00:00+0000\",\"standardWording\": \"NCCADBCM\",\"categoryId\": \"Maestro-betaling\",\"transactionAmount\": -1.94,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE54000000000000\",\"counterpartyName\": \"7-ELEVEN 4217107\",\"accountingReference\": \"B8H20NC0030002BY\",\"creditDebitIndicator\": \"DBIT\",\"operationReference\": \"B8H20NC0030002BY\",\"sortField\": \"0000003\",\"identifier\": \"20180820µB8H20NC0030002BYµBE92000450043523µ0000003\"}, {\"bookingDateTime\": \"2018-08-20T00:00:00+0000\",\"standardWording\": \"NCCADBCM\",\"categoryId\": \"Maestro-betaling\",\"transactionAmount\": -1.94,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"BE54000000000000\",\"counterpartyName\": \"NORRA BANTORGETS KIOSK\",\"accountingReference\": \"B8H20NC0030002BX\",\"creditDebitIndicator\": \"DBIT\",\"operationReference\": \"B8H20NC0030002BX\",\"sortField\": \"0000002\",\"identifier\": \"20180820µB8H20NC0030002BXµBE92000450043523µ0000002\"}, {\"bookingDateTime\": \"2018-08-14T00:00:00+0000\",\"standardWording\": \"ZPREITTF\",\"categoryId\": \"Ontv. order klant-trsf\",\"transactionAmount\": 42.35,\"transactionCurrency\": \"EUR\",\"counterpartyAccount\": \"52021013014\",\"counterpartyName\": \"TINK AB\",\"accountingReference\": \"B8H10RETYW42E1ZQ\",\"creditDebitIndicator\": \"CRDT\",\"operationReference\": \"B8H10RETYW42E1ZQ\",\"sortField\": \"0000001\",\"identifier\": \"20180814µB8H14REBTW3QP4B5µBE92000450043523µ0000001\"}]";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/operations"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        RequestBuilder requestBuilder =
                httpClient.request(getOrigin() + AccountTransactionsRequest.URL_PATH);
        AccountTransactionsRequest objectUnderTest =
                new AccountTransactionsRequest(authContext, 1, 21, "BE68539007547034");
        // when
        List<BPostBankTransactionDTO> result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals(8, result.size());
        BPostBankTransactionDTO dto = result.get(0);
        Assert.assertEquals("2019-12-11T00:00:00+0000", dto.bookingDateTime);
        Assert.assertEquals("Ontv. order klant-trsf", dto.categoryId);
        Assert.assertEquals(new BigDecimal(50), dto.transactionAmount);
        Assert.assertEquals("EUR", dto.transactionCurrency);
        Assert.assertEquals("51238201814", dto.counterpartyAccount);
        Assert.assertEquals("TINK AB", dto.counterpartyName);
        Assert.assertEquals("1234567890123456789012345678901234567890", dto.identifier);
    }
}
