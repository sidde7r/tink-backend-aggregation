package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.NordeaSeBusinessApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;

public class BankStatusIdTest {
    NordeaSeBusinessApiClient apiClient;
    PersistentStorage persistentStorage;
    TinkHttpClient client;
    AgentComponentProvider componentProvider;
    QsealcSigner qsealcSigner;
    NordeaSeBusinessDecoupledAuthenticator nordeaSeBusinessDecoupledAuthenticator;
    String companyId;
    DecoupledAuthenticationResponse authenticationResponse;
    CredentialsRequest credentialsRequest;
    LocalDateTime localDateTime = LocalDateTime.parse("1995-11-05T10:11:30");

    @Before
    public void setUp() {
        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
        authenticationResponse = mock(DecoupledAuthenticationResponse.class);
        persistentStorage = mock(PersistentStorage.class);
        client = mock(TinkHttpClient.class);
        componentProvider = mock(AgentComponentProvider.class);
        credentialsRequest = mock(CredentialsRequest.class);
        when(componentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);
        UserAvailability ua = new UserAvailability();
        ua.setUserAvailableForInteraction(true);
        ua.setUserPresent(true);
        when(componentProvider.getCredentialsRequest().getUserAvailability()).thenReturn(ua);
        when(componentProvider.getLocalDateTimeSource()).thenReturn(localDateTimeSource);
        when(componentProvider.getLocalDateTimeSource().now()).thenReturn(localDateTime);
        qsealcSigner = mock(QsealcSigner.class);
        apiClient =
                new NordeaSeBusinessApiClient(
                        componentProvider, client, persistentStorage, qsealcSigner);
        companyId = "1111111";
        nordeaSeBusinessDecoupledAuthenticator =
                new NordeaSeBusinessDecoupledAuthenticator(apiClient, companyId);
    }

    @Test
    public void shouldReturnExpiredAutostartTokenAndThereAreErrors() {
        BankIdStatus result =
                nordeaSeBusinessDecoupledAuthenticator
                        .getBankIdStatusWithoutAuthenticationResponse();

        Assert.assertEquals(BankIdStatus.EXPIRED_AUTOSTART_TOKEN, result);
    }

    @Test
    public void shouldReturnWaitingWhenAuthResponseIsAssignmentPending() {
        when(authenticationResponse.getStatus()).thenReturn("assignment_pending");
        BankIdStatus result =
                nordeaSeBusinessDecoupledAuthenticator
                        .getBankIdStatusWithSuccessfulAuthenticationResponse(
                                authenticationResponse);

        Assert.assertEquals(BankIdStatus.WAITING, result);
    }

    @Test
    public void shouldReturnWaitingWhenAuthResponseIsConfirmationPending() {
        when(authenticationResponse.getStatus()).thenReturn("confirmation_pending");
        BankIdStatus result =
                nordeaSeBusinessDecoupledAuthenticator
                        .getBankIdStatusWithSuccessfulAuthenticationResponse(
                                authenticationResponse);

        Assert.assertEquals(BankIdStatus.WAITING, result);
    }

    @Test
    public void shouldReturnFailedUnknownWhenAuthResponseIsRandom() {
        when(authenticationResponse.getStatus()).thenReturn("random");
        BankIdStatus result =
                nordeaSeBusinessDecoupledAuthenticator
                        .getBankIdStatusWithSuccessfulAuthenticationResponse(
                                authenticationResponse);

        Assert.assertEquals(BankIdStatus.FAILED_UNKNOWN, result);
    }
}
