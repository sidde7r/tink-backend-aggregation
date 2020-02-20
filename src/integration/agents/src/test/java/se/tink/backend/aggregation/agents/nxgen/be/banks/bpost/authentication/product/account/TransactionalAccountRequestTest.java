package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.WireMockIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

@Ignore
public class TransactionalAccountRequestTest extends WireMockIntegrationTest {

    private static final String LOGIN = "12345678";
    private BPostBankAuthContext authContext;

    @Before
    public void init() {
        authContext = Mockito.mock(BPostBankAuthContext.class);
        Mockito.when(authContext.getLogin()).thenReturn(LOGIN);
    }

    @Test
    public void withBodyShouldCreteExpectedRequestBody() throws JSONException {
        // given
        RequestBuilder requestBuilder = Mockito.mock(RequestBuilder.class);
        TransactionalAccountRequest objectUnderTest = new TransactionalAccountRequest(authContext);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        // when
        objectUnderTest.withBody(requestBuilder);
        // then
        Mockito.verify(requestBuilder).body(stringArgumentCaptor.capture());
        String body = stringArgumentCaptor.getValue();
        JSONObject bodyAsJosn = new JSONObject(body);
        Assert.assertEquals(
                authContext.getLogin(), bodyAsJosn.getString("principalIdentification"));
    }

    @Test
    public void executeShouldReturnProperResponseObject() throws RequestException {
        // given
        final String responseBody =
                "{\"current-account\": [{\"accountIdentification\": [{\"scheme\": \"IBAN\",\"id\": \"BE68539007547034\"}],\"product\": \"b.compact rekening\",\"bankProduct\": \"300120\",\"currency\": \"EUR\",\"alias\": \"account alias\",\"availableBalance\": \"85.53\",\"bookedBalance\": \"84.53\",\"isSightAccount\": true,\"isAccountReadOnly\": false,\"clientShortNameHolder\": \"Name Surname\"}],\"deposit-account\": []}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/accounts"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
        RequestBuilder requestBuilder =
                httpClient.request(getOrigin() + TransactionalAccountRequest.URL_PATH);
        TransactionalAccountRequest objectUnderTest = new TransactionalAccountRequest(authContext);
        // when
        BPostBankAccountsResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals(1, result.currentAccounts.size());
        BPostBankAccountDTO accountDTO = result.currentAccounts.get(0);
        Assert.assertEquals("85.53", accountDTO.availableBalance);
        Assert.assertEquals("84.53", accountDTO.bookedBalance);
        Assert.assertEquals("EUR", accountDTO.currency);
        Assert.assertEquals("Name Surname", accountDTO.clientName);
        Assert.assertEquals("IBAN", accountDTO.accountIdentification.get(0).scheme);
        Assert.assertEquals("BE68539007547034", accountDTO.accountIdentification.get(0).id);
        Assert.assertEquals("account alias", accountDTO.alias);
    }
}
