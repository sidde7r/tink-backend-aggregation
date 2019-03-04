package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateOtpChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.RegisterUserResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

import java.util.UUID;

public class AxaApiClientTest {

    @Test
    public void testGenerateChallengeResponse() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.setDebugOutput(true);
        httpClient.setCipherSuites(AxaConstants.CIPHER_SUITES);

        AxaApiClient client = new AxaApiClient(httpClient);

        String ucrid = "77777777777777777777777777777777";
        GenerateChallengeResponse response =
                client.postGenerateChallenge(AxaConstants.Request.BASIC_AUTH, ucrid);

        Assert.assertEquals(8, response.getChallenge().length());
        Assert.assertEquals(6, response.getActivationPassword().length());
    }

    @Test
    public void testRegisterUserFail() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.setDebugOutput(true);
        httpClient.setCipherSuites(AxaConstants.CIPHER_SUITES);

        AxaApiClient client = new AxaApiClient(httpClient);

        String ucrid = "77777777777777777777777777777777";
        String pan = "77777777777777777N0";
        String challenge = "88888888";
        String challengeResponse = "99999999";
        String clientInitialVector = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        String encryptedClientPublicKeyAndNonce =
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        UUID deviceId = UUID.fromString("c4419a66-d0f8-4362-9492-149af47cca3a");
        RegisterUserResponse response =
                client.postRegisterUser(
                        AxaConstants.Request.BASIC_AUTH,
                        ucrid,
                        deviceId,
                        pan,
                        challenge,
                        challengeResponse,
                        clientInitialVector,
                        encryptedClientPublicKeyAndNonce);

        Assert.assertEquals(1, response.getMsgcd().size());
        Assert.assertEquals("HB1004", response.getMsgcd().iterator().next());
    }

    @Test
    public void testGenerateOtpChallenge() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.setDebugOutput(true);
        httpClient.setCipherSuites(AxaConstants.CIPHER_SUITES);

        AxaApiClient client = new AxaApiClient(httpClient);

        String serialNo = "5f77ad78-dd1d-4d9a-bdcf-36f7d1079cb1";
        GenerateOtpChallengeResponse response =
                client.postGenerateOtpChallenge(AxaConstants.Request.BASIC_AUTH, serialNo);

        Assert.assertEquals(16, response.getChallenge().length());
    }

    @Test
    public void testLogon() {
        TinkHttpClient httpClient = new TinkHttpClient();
        httpClient.setDebugOutput(true);
        httpClient.setCipherSuites(AxaConstants.CIPHER_SUITES);

        AxaApiClient client = new AxaApiClient(httpClient);

        String username = "5f77ad78-dd1d-4d9a-bdcf-36f7d1079cb1";
        String password = "1846712417156575";
        String deviceId = "c4419a66-d0f8-4362-9492-149af47cca3a";
        client.postLogon(AxaConstants.Request.BASIC_AUTH, deviceId, username, password);
    }
}
