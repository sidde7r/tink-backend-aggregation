package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.indentitydata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK2;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.EBK_SSO;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.GESTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.JSESSIONID;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.LOCATION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.REFERER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Identity.REFERER_WEBSITE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_INFO;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_INIT_TRANSFER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls.IDENTITY_TOKEN;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.BankinterIdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.NameElement.Type;

public class BankinterIdentityDataFetcherTest {

    public static final String JSESSIONID_RECEIVE_TICKET_VALUE =
            "0000-JI7HqXfQEjg-JYINcrNdFD:1e5pipgl8";
    public static final String EBK2_RECEIVE_TICKET_VALUE = "85c04083aa08f5dd11ebbb1b2282ea7278e5";
    public static final String EBK_VALUE = "13SI8CXE00OMMFUM1628161605902";
    public static final String EBK_SSO_VALUE = "7cf40458-34aa-44b8-859a-28dbadb47f20";
    public static final String JSESSIONID_VALUE = "0000YSWM3dHqmDEkSAP5lzhvvFS:1ajerqs8g";
    public static final String CREATE_TOKEN_URL =
            "https://bancaonline.bankinter.com/gestion/services/auth/transfer/create-token?site=seguridad-web&state=0917a7de-78cc-4f8c-86b8-88c04a8458a4";
    public static final String RECEIVE_TICKET_URL =
            "https://seguridad.bankinter.com/gestion/transfer/receive-ticket?ticket=26Egong22CcVDq8y7E10wt90cOA%3D&state=0917a7de-78cc-4f8c-86b8-88c04a8458a4";
    public static final String JSESSIONID_SECURITY = "0000tee3RikKy6pndId2r43K8wU:1e5pipgl8";
    public static final String ID = "aff97QhWwaQ5qqmn";
    public static final String IDENTITY_INFO_RESPONSE =
            "{\"id\":\"aff97QhWwaQ5qqmn\",\"identificationDoc\":{\"type\":\"DNI\",\"number\":\"00000000L\"},\"personType\":\"NATURAL\",\"segment\":\"PARTICULAR\",\"personalData\":{\"name\":\"NAME\",\"surnames\":\"SURNAME1 SURNAME2\",\"nationality\":\"ESPAÑA\",\"gender\":\"MALE\",\"birthdate\":\"1994-02-15\",\"birthCountry\":\"ES\",\"residenceCountry\":\"ES\",\"foreignTaxesFlag\":false,\"profession\":\"PROGRAMADOR INFORMATICO\",\"maritalStatus\":\"SOLTERO\",\"matrimonialRegime\":\"SIN REGIMEN\",\"numberOfChildren\":0},\"accessibilityData\":{\"disability\":false,\"disabilityPercentage\":0.00,\"hearingDisability\":\"No padece discapacidad auditiva\",\"brailleInd\":false,\"signLanguageInd\":false,\"visualDisability\":\"No padece discapacidad visual\",\"mentalDisability\":\"No padece discapacidad psiquica\",\"physicalDisability\":\"No padece discapacidad fisica\",\"coordinatesTypes\":\"PIN PAD\",\"certifiedType\":\"000\"},\"contactData\":{\"telephones\":[{\"type\":\"FIXED\",\"prefix\":\"34\",\"number\":910000000},{\"type\":\"MOBILE\",\"prefix\":\"34\",\"number\":600000000,\"forOTP\":true}],\"email\":\"adrian@navarro.me\",\"taxAddress\":{\"streetName\":\"nostreet,2\",\"roadType\":\"CALLE\",\"floor\":\"0\",\"postalCode\":28000,\"city\":\"Madrid\",\"province\":\"MADRID\",\"countryCode\":\"ES\"},\"postalAddress\":{\"streetName\":\"nostreet,2\",\"roadType\":\"CALLE\",\"floor\":\"0\",\"postalCode\":28000,\"city\":\"Madrid\",\"province\":\"MADRID\",\"countryCode\":\"ES\"},\"operativeLanguage\":\"No informado\",\"mailLanguage\":\"es\",\"mailType\":\"DIGITAL\"}}";
    public static final String TOKEN_INFO_RESPONSE =
            "{\"id\":\"aff97QhWwaQ5qqmn\",\"companyprofile\":false,\"personalData\":{\"name\":\"NAME SURNAME1 SURNAME2\",\"firstname\":\"NAME\",\"surnames\":\"SURNAME1 SURNAME2\"},\"category\":\"CLIENT\",\"categoryType\":\"INDIVIDUAL\",\"categorySubtype\":\"INDIVIDUAL\"}";
    public static final String DNI_VALUE = "00000000L";
    public static final String NAME = "NAME";
    public static final String SURNAMES = "SURNAME1 SURNAME2";

    private BankinterIdentityDataFetcher bankinterIdentityDataFetcher;

    private TinkHttpClient tinkHttpClient;

    @Before
    public void setup() throws URISyntaxException {

        tinkHttpClient = mock(TinkHttpClient.class);
        BankinterApiClient bankinterApiClient = new BankinterApiClient(tinkHttpClient);
        bankinterIdentityDataFetcher = new BankinterIdentityDataFetcher(bankinterApiClient);

        BasicClientCookie jsessionCookie = new BasicClientCookie(JSESSIONID, JSESSIONID_VALUE);
        jsessionCookie.setPath(GESTION);

        when(tinkHttpClient.getCookies())
                .thenReturn(
                        Arrays.asList(
                                new BasicClientCookie(EBK, EBK_VALUE),
                                jsessionCookie,
                                new BasicClientCookie(EBK_SSO, EBK_SSO_VALUE)));
    }

    @Test
    public void testIdentityDataRequest() {

        // given
        mockInitTransferRequest();
        mockCreateTokenRequest();
        mockReceiveTicketRequest();
        mockTokenIdentityRequest();
        mockIdentityInfoRequest();

        // when
        IdentityData indentityData = bankinterIdentityDataFetcher.fetchIdentityData();

        // then
        verify(tinkHttpClient, times(1)).request(IDENTITY_INIT_TRANSFER);
        verify(tinkHttpClient, times(1)).request(CREATE_TOKEN_URL);
        verify(tinkHttpClient, times(1)).request(RECEIVE_TICKET_URL);
        verify(tinkHttpClient, times(1)).request(IDENTITY_TOKEN);
        verify(tinkHttpClient, times(1)).request(IDENTITY_INFO + ID);

        Assert.assertEquals(DNI_VALUE, indentityData.getSsn());
        Assert.assertEquals(LocalDate.of(1994, 02, 15), indentityData.getDateOfBirth());
        Assert.assertEquals(Type.FIRST_NAME, indentityData.getNameElements().get(0).getType());
        Assert.assertEquals(NAME, indentityData.getNameElements().get(0).getValue());
        Assert.assertEquals(Type.SURNAME, indentityData.getNameElements().get(1).getType());
        Assert.assertEquals(SURNAMES, indentityData.getNameElements().get(1).getValue());
    }

    private void mockInitTransferRequest() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getCookies())
                .thenReturn(
                        Arrays.asList(
                                new NewCookie(JSESSIONID, JSESSIONID_RECEIVE_TICKET_VALUE),
                                new NewCookie(EBK2, EBK2_RECEIVE_TICKET_VALUE)));

        MultivaluedMap headers = new MultivaluedMapImpl();
        headers.add(LOCATION, CREATE_TOKEN_URL);

        when(httpResponse.getHeaders()).thenReturn(headers);

        when(tinkHttpClient.request(IDENTITY_INIT_TRANSFER)).thenReturn(requestBuilder);
        when(requestBuilder.header(REFERER, REFERER_WEBSITE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK, EBK_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(httpResponse);
    }

    private void mockCreateTokenRequest() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        MultivaluedMap headers = new MultivaluedMapImpl();
        headers.add(LOCATION, RECEIVE_TICKET_URL);

        when(httpResponse.getHeaders()).thenReturn(headers);

        when(tinkHttpClient.request(CREATE_TOKEN_URL)).thenReturn(requestBuilder);
        when(requestBuilder.header(REFERER, REFERER_WEBSITE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK, EBK_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(JSESSIONID, JSESSIONID_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK_SSO, EBK_SSO_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(httpResponse);
    }

    private void mockReceiveTicketRequest() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getCookies())
                .thenReturn(Arrays.asList(new NewCookie(JSESSIONID, JSESSIONID_SECURITY)));

        when(tinkHttpClient.request(RECEIVE_TICKET_URL)).thenReturn(requestBuilder);
        when(requestBuilder.header(REFERER, REFERER_WEBSITE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK, EBK_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(JSESSIONID, JSESSIONID_RECEIVE_TICKET_VALUE))
                .thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK2, EBK2_RECEIVE_TICKET_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(httpResponse);
    }

    private void mockTokenIdentityRequest() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getBody(String.class)).thenReturn(TOKEN_INFO_RESPONSE);

        when(tinkHttpClient.request(IDENTITY_TOKEN)).thenReturn(requestBuilder);
        when(requestBuilder.header(REFERER, REFERER_WEBSITE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK, EBK_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(JSESSIONID, JSESSIONID_SECURITY)).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(httpResponse);
    }

    private void mockIdentityInfoRequest() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getBody(String.class)).thenReturn(IDENTITY_INFO_RESPONSE);

        when(tinkHttpClient.request(IDENTITY_INFO + ID)).thenReturn(requestBuilder);
        when(requestBuilder.header(REFERER, REFERER_WEBSITE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(EBK, EBK_VALUE)).thenReturn(requestBuilder);
        when(requestBuilder.cookie(JSESSIONID, JSESSIONID_SECURITY)).thenReturn(requestBuilder);
        when(requestBuilder.get(any())).thenReturn(httpResponse);
    }
}
