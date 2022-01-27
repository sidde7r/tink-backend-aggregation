package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankBankIdSigner;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

public class SwedbankBankIdSignerTest {

    private SwedbankApiClient apiClient;
    private AuthenticationResponse authenticationResponse;
    private AuthenticationResponse scaResponseInterrupt;
    private AuthenticationResponse scaResponseFail;
    private SwedbankBankIdSigner bankIdSigner;
    private PaymentRequest paymentRequest;

    @Before
    public void setUp() throws JsonProcessingException {
        apiClient = mock(SwedbankApiClient.class);
        authenticationResponse = setUpAuthResponse();
        bankIdSigner = new SwedbankBankIdSigner(apiClient);
        bankIdSigner.setAuthenticationResponse(authenticationResponse);
        paymentRequest = mock(PaymentRequest.class);
        scaResponseInterrupt = setUpScaResponseInterruption();
        scaResponseFail = setUpScaResponseFail();
    }

    @Test
    public void shouldReturnInterruptedIfScaResponseIsInterrupted() {

        when(apiClient.getScaResponse(authenticationResponse.getCollectAuthUri()))
                .thenReturn(scaResponseInterrupt);

        BankIdStatus result = bankIdSigner.collect(paymentRequest);

        Assert.assertEquals(result, BankIdStatus.INTERRUPTED);
    }

    @Test
    public void shouldReturnExpiredTokenIfScaResponseIsFailed() {

        when(apiClient.getScaResponse(authenticationResponse.getCollectAuthUri()))
                .thenReturn(scaResponseFail);

        BankIdStatus result = bankIdSigner.collect(paymentRequest);

        Assert.assertEquals(result, BankIdStatus.EXPIRED_AUTOSTART_TOKEN);
    }

    private AuthenticationResponse setUpAuthResponse() throws JsonProcessingException {
        String jsonString =
                "{\"psuMessage\" : \"Loging via Tink_org\",\"challengeData\": { \"autoStartToken\": \"f72c647f-72f5-4350-9a1b-111c20b8c7c8\" },\"scaStatus\": \"started\", \"_links\": {\"scaStatus\": {\"href\": \"/psd2/v4/authorize-decoupled/authorize/e5a6ab85-031a-45aa-a998-19176ff89eb9\"}}}";
        return new ObjectMapper().readValue(jsonString, AuthenticationResponse.class);
    }

    private AuthenticationResponse setUpScaResponseInterruption() throws JsonProcessingException {
        String jsonString =
                "{\"tppMessages\":[{\"category\":\"INFO\",\"code\":\"USER_INTERUPTION\",\"text\":\"PSU must add recipient to recipient list first.\"}]}";
        return new ObjectMapper().readValue(jsonString, AuthenticationResponse.class);
    }

    private AuthenticationResponse setUpScaResponseFail() throws JsonProcessingException {
        String jsonString = "{\"scaStatus\": \"failed\"}";
        return new ObjectMapper().readValue(jsonString, AuthenticationResponse.class);
    }
}
