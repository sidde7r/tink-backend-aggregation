package se.tink.backend.main.controllers.abnamro;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbnAmroAccountControllerTest {

    @Mock
    SystemServiceFactory systemServiceFactory;

    @Mock
    UpdateService updateService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalStateException.class)
    public void testNonAbnCluster() {

        AbnAmroAccountController controller = new AbnAmroAccountController(Cluster.CORNWALL, systemServiceFactory);

        controller.updateAccounts(null, null);
    }

    @Test
    public void testUpdateAccountAtAbnAmroCluster() {

        when(systemServiceFactory.getUpdateService()).thenReturn(updateService);

        Credentials credentials = new Credentials();
        credentials.setId("credentialsId");
        credentials.setUserId("userId");

        Account validAccount = new Account();
        validAccount.setCredentialsId(credentials.getId());
        validAccount.setUserId(credentials.getUserId());

        AbnAmroAccountController controller = new AbnAmroAccountController(Cluster.ABNAMRO, systemServiceFactory);

        controller.updateAccounts(credentials, ImmutableList.of(validAccount));

        // Verify that the update service has been called
        verify(updateService).updateAccount(any(UpdateAccountRequest.class));
    }
}
