package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.WireMockIntegrationTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LoginPINAuthRequestTest extends WireMockIntegrationTest {

    private BPostBankAuthContext authContextMock;
    private RequestBuilder requestBuilder;

    @Before
    public void init() {
        authContextMock = Mockito.mock(BPostBankAuthContext.class);
        requestBuilder = httpClient.request(getOrigin() + LoginPINAuthRequest.URL_PATH);
    }

    @Test
    public void withBodyShouldCreateCorrectRequestBody() throws JSONException {
        // given
        mockAuthContext();
        LoginPINAuthRequest objectUnderTest = new LoginPINAuthRequest(authContextMock);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        // when
        objectUnderTest.withBody(requestBuilderMock);
        // then
        Mockito.verify(requestBuilderMock).body(stringArgumentCaptor.capture());
        JSONObject body = new JSONObject(stringArgumentCaptor.getValue());
        Assert.assertEquals(
                authContextMock.getDataMapCode(), body.getJSONObject("dataMap").getString("code"));
        Assert.assertEquals(
                authContextMock.getLogin(), body.getJSONObject("user").getString("loginName"));
        Assert.assertEquals(
                authContextMock.getDeviceRootedHash(), body.getString("deviceRootedHash"));
    }

    @Test
    public void executeShouldReturnSuccessLoginResponseDto() throws RequestException {
        // given
        final String jsonBody =
                "{\"UserSessionImpl\": {\"orderReference\": \"C0B11CWXH5793ZWI\",\"state\": \"4000\",\"isError\": false,\"credentials\": {},\"sessionId\": \"k5_l1FbANE2vOEI2MtMB6l4BF5rKADaExeHDNeK3\",\"roles\": [\"group_majority\", \"group_authenticated\", \"group_b2c_authenticated\", \"group_u_web_cccc01\", \"group_u_web_cgcr02\", \"group_u_web_bicu03\", \"group_u_web_pgcr0b\", \"group_u_web_smcl01\", \"group_u_web_smcd02\", \"group_u_web_cbcl01\", \"group_u_web_smcu01\", \"group_u_web_ctcl01\", \"group_u_web_pgcc01\", \"group_u_web_tdcr01\", \"group_u_web_cgcl02\", \"group_u_web_tdcc01\", \"group_u_web_dvcr01\", \"group_u_web_bicu0k\", \"group_u_web_smcu02\", \"group_u_web_srcc01\", \"group_u_web_smcc01\", \"group_u_web_bicc01\", \"group_u_web_srcl01\", \"group_u_web_bicr01\", \"group_u_web_smcl02\", \"group_u_web_recr01\", \"group_u_web_tdcl01\", \"group_u_web_srcu01\", \"group_u_web_pgcr1a\", \"group_u_web_pgcl0a\", \"group_u_web_bicu01\", \"group_u_web_pgcu1b\", \"group_u_web_dvcl02\", \"group_u_web_srcd01\", \"group_u_web_pgcr01\", \"group_u_web_bicl01\", \"group_u_web_bicr0k\", \"group_u_web_recc01\", \"group_u_web_srcr01\", \"group_u_web_bicd01\", \"group_u_web_pgcd01\", \"group_u_web_cccl01\", \"group_u_web_cggl01\", \"group_u_web_cggr01\", \"group_u_web_pggc03\", \"group_u_web_ptgl01\", \"group_u_web_bigl01\", \"group_u_web_ccgl01\", \"group_u_web_ccgrx1\", \"group_u_web_pggc02\", \"group_u_web_pggc01\", \"group_u_web_bigr01\", \"group_u_web_cwgu01\", \"group_u_web_pggr01\", \"group_u_web_pggd01\", \"group_u_web_pggl01\", \"group_u_web_bigd01\", \"group_u_web_bigu01\", \"group_u_web_bigc01\", \"group_u_web_incr03\", \"group_u_web_pgcl02\", \"group_u_web_pggcz1\", \"group_u_web_pggcz2\", \"group_u_web_pggcz3\", \"group_u_web_pggcy3\", \"group_u_web_pggcy2\", \"group_u_web_mcccp1\", \"group_u_web_pggcy1\", \"group_u_web_mccrp1\", \"group_u_web_mccdp1\", \"group_u_web_clcd01\", \"group_u_web_clcc01\", \"group_u_web_clcu01\", \"group_u_web_clcl02\", \"group_u_web_clcr01\", \"group_u_web_bicrz2\", \"group_u_web_zecr01\", \"group_u_web_mscrz1\", \"group_u_web_mscrz2\", \"group_u_web_msclz2\", \"group_u_web_msclz1\", \"group_u_web_zecl01\", \"group_u_web_mscuz1\", \"group_u_web_zxcr10\", \"group_u_web_zxcr13\", \"group_u_web_epcr01\", \"group_u_web_epcl01\", \"group_u_web_zxcr12\", \"group_u_web_zxcr15\", \"group_u_web_zxcr14\", \"group_u_web_zxcr17\", \"group_u_web_zxcr16\", \"group_u_web_zxcr05\", \"group_u_web_zxcr09\", \"group_u_web_zxcc06\", \"group_u_web_zxcc09\", \"group_u_web_zxcc08\", \"group_u_web_zxcc11\", \"group_u_web_zxcc15\", \"group_u_web_zxcl14\", \"group_u_web_zxcl16\", \"group_u_web_zxcl09\", \"group_u_web_zxcu12\", \"group_u_web_zxcu15\", \"group_u_web_zxcd08\", \"group_u_web_zxcd09\", \"group_u_web_zxcu05\", \"group_u_web_zxcu09\", \"group_u_web_zxcu06\", \"group_u_web_mucd01\", \"group_u_web_mucu01\", \"group_u_web_mucl01\", \"group_u_web_mucc01\", \"group_u_web_zxcr19\", \"group_u_web_zxcr18\", \"group_u_web_zxcr20\", \"group_u_web_zxcr22\", \"group_u_web_mucr01\", \"group_u_web_zxcr24\", \"group_u_web_zxcc20\", \"group_u_web_zxcc23\", \"group_u_web_zxcc25\", \"group_u_web_zxcc26\", \"group_u_web_zxcl19\", \"group_u_web_zxcl18\", \"group_u_web_zxcl20\", \"group_u_web_zxcl22\", \"group_u_web_zxcl21\", \"group_u_web_zxcl24\", \"group_u_web_zxcl23\", \"group_u_web_zxcd20\", \"group_u_web_zxcd22\", \"group_u_web_zxcu27\", \"ROLE_SERVICES\", \"ROLE_USER\", \"group_user\"],\"language\": \"nl\",\"securityContext\": {\"authenticationType\": \"PIN\",\"businessChannel\": \"B2C\",\"communicationMode\": \"MOB\",\"company\": \"1\",\"project\": \"SOPRABANKING\",\"deviceInstallationID\": \"4TAOD0R2I278HHCI9EPN33HX6KY2YJ5B3RN3MPG812NK3GKT3SQFIX3YEQND2DS0\",\"deviceType\": \"SPH\",\"deviceUniqueID\": \"CF07551B-F88E-4BEB-BDFA-94343AA7D077\",\"deviceRootedHash\": \"92dbbe7497536c95320abad4c5485fecb94bffd21565a3cb0a7395ac05dc4e81\"},\"contentLocked\": false,\"identificationToken\": {},\"reference\": \"C0B11CWXH5793ZWI\",\"auditData\": {\"startDate\": \"2020-02-11T12:59:29.476+0000\"},\"reportAvailable\": false,\"sessionToken\": \"HYFVFKI5W1ORU4GP7XL4RWF196TGVRYM\",\"visitor\": {\"birthDate\": \"1994-09-24T22:00:00.000+0000\",\"country\": \"België\",\"firstName\": \"Wouter\",\"houseNumber\": \"28\",\"lastName\": \"Vertessen\",\"postCode\": \"2800\",\"streetName\": \"Elisabeth Desoleilstraat\",\"thirdPartyNumber\": \"0010YNU9\",\"town\": \"Mechelen\"},\"feedbackList\": {\"feedbacks\": []},\"user\": {\"userId\": \"01975233\",\"loginName\": \"01975233\"}}}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/loginPINauth"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(jsonBody)
                                        .withHeader("Content-Type", "application/json")));
        mockAuthContext();
        LoginPINAuthRequest objectUnderTest = new LoginPINAuthRequest(authContextMock);
        // when
        LoginResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertFalse(result.isError());
        Assert.assertEquals("4000", result.getState());
        Assert.assertEquals("k5_l1FbANE2vOEI2MtMB6l4BF5rKADaExeHDNeK3", result.getSessionId());
        Assert.assertEquals("HYFVFKI5W1ORU4GP7XL4RWF196TGVRYM", result.getSessionToken());
        Assert.assertFalse(result.isMobileAccessDeletedError());
    }

    @Test(expected = RequestException.class)
    public void executeShouldReturnMobileAccessDeletedLoginResponseDto() throws RequestException {
        // given
        final String jsonBody =
                "{\"localizedMessage\":\"Uw mobiele banktoegang werd verwijderd en is niet langer bruikbaar. (XXXX1668)\",\"errorCode\":\"XXXX1668\"}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/bpb/services/rest/v2/loginPINauth"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(jsonBody)
                                        .withHeader("Content-Type", "application/json")));
        mockAuthContext();
        LoginPINAuthRequest objectUnderTest = new LoginPINAuthRequest(authContextMock);
        // when
        LoginResponseDTO result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertTrue(result.isError());
        Assert.assertTrue(result.isMobileAccessDeletedError());
    }

    private void mockAuthContext() {
        final String login = "12345678";
        final String pin = "111111";
        final String deviceRootedHash =
                "ad6d7808c8f9a7bedba525c6e3754775bfa4c9fbb0fe42339e3bebdb47f149dc";
        final String dataMapCode =
                "fb953df02d8249123f8e0aad793cbdf9fb7b947f8dae9c65e934e4990ce8f6eb";
        Mockito.when(authContextMock.getPin()).thenReturn(pin);
        Mockito.when(authContextMock.getLogin()).thenReturn(login);
        Mockito.when(authContextMock.getDeviceRootedHash()).thenReturn(deviceRootedHash);
        Mockito.when(authContextMock.getDataMapCode()).thenReturn(dataMapCode);
    }
}
