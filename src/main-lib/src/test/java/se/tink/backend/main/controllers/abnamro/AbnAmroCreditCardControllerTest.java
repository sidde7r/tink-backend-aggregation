package se.tink.backend.main.controllers.abnamro;

import java.util.Collections;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.cluster.Cluster;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbnAmroCreditCardControllerTest {

    @Mock CredentialsRepository credentialsRepository;
    @Mock CredentialsRequestRunnableFactory credentialsRequestRunnableFactory;
    @Mock AbnAmroAccountController abnAmroAccountController;
    @Mock Provider<ServiceContext> serviceContextProvider;
    @Mock ServiceContext serviceContext;
    @Mock ServiceFactory serviceFactory;
    @Mock CredentialsService credentialsService;
    @Mock AbnAmroConfiguration abnAmroConfiguration;
    AbnAmroCreditCardController controller;

    @Before
    public void setUp() {
        controller = new AbnAmroCreditCardController(Cluster.ABNAMRO, credentialsRepository,
                credentialsRequestRunnableFactory, abnAmroAccountController, serviceContextProvider,
                abnAmroConfiguration);
        when(serviceContextProvider.get()).thenReturn(serviceContext);
        when(serviceContext.getServiceFactory()).thenReturn(serviceFactory);
        when(serviceFactory.getCredentialsService()).thenReturn(credentialsService);
    }

    @Test
    public void testUpdateCredentials() {
        User user = new User();
        user.setId("userId");
        user.setUsername(AbnAmroLegacyUserUtils.getUsername("1245"));
        AuthenticatedUser authUser = new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user);

        Account account = new Account();
        account.setUserId("userId");
        account.setType(AccountTypes.CREDIT_CARD);
        account.setAccountNumber("1234567891111111");

        when(credentialsService.create(eq(authUser), (OAuth2ClientRequest) isNull(), any(Credentials.class),
                eq(Collections.emptySet())))
                .thenReturn(new Credentials());

        Optional<Credentials> credentials = controller.updateCredentials(authUser, Lists.newArrayList(account));

        assertThat(credentials.isPresent()).isTrue();

        // Verify that we have update the accounts
        verify(abnAmroAccountController).updateAccounts(eq(credentials.get()), Mockito.<Account>anyList());
    }

    @Test
    public void testThatIcsCredentialsAreRefreshedIfAccountIsActivated() {
        Credentials credentials = AbnAmroIcsCredentials.create("userId", "1234").getCredentials();
        credentials.setId("credentialsId");

        when(credentialsRepository.findOne(eq("credentialsId"))).thenReturn(credentials);
        Runnable refreshCredentialsRunnable = mock(Runnable.class);
        when(credentialsRequestRunnableFactory
                .createRefreshRunnable(any(User.class), any(Credentials.class), anyBoolean(), anyBoolean(),
                        anyBoolean()))
                .thenReturn(refreshCredentialsRunnable);

        User user = new User();
        user.setId("userId");

        Account account = new Account();
        account.setExcluded(false);
        account.setCredentialsId("credentialsId");
        account.setUserId("userId");
        account.setBankId("1234");

        controller.accountUpdated(user, account);

        // Verify that the credential is updated
        verify(credentialsRepository).save(credentials);

        // Verify that we have executed the update
        verify(serviceContext).execute(refreshCredentialsRunnable);
    }

}

