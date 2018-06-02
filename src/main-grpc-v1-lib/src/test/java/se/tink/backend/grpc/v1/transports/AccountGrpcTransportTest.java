package se.tink.backend.grpc.v1.transports;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.interceptors.ExceptionInterceptor;
import se.tink.backend.grpc.v1.utils.TestAuthenticationInterceptor;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.rpc.ListAccountsRequest;
import se.tink.grpc.v1.rpc.ListAccountsResponse;
import se.tink.grpc.v1.rpc.UpdateAccountRequest;
import se.tink.grpc.v1.rpc.UpdateAccountResponse;
import se.tink.grpc.v1.services.AccountServiceGrpc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountGrpcTransportTest {
    private static final String SERVER_NAME = "in-process server for " + AccountGrpcTransportTest.class;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private ManagedChannel inProcessChannel;
    private Server inProcessServer;
    private AccountServiceGrpc.AccountServiceBlockingStub accountService;

    @Before
    public void setUp() throws IOException {
        AccountServiceController accountServiceController = mockAccountController();
        inProcessServer = InProcessServerBuilder.forName(SERVER_NAME)
                .addService(ServerInterceptors.intercept(
                        new AccountGrpcTransport(accountServiceController, mock(CredentialServiceController.class)),
                        new ExceptionInterceptor(),
                        new TestAuthenticationInterceptor(mockAuthenticationContext())))
                .directExecutor()
                .build().start();
        inProcessChannel =
                InProcessChannelBuilder.forName(SERVER_NAME).directExecutor().build();
        accountService = AccountServiceGrpc.newBlockingStub(inProcessChannel);
    }

    private AccountServiceController mockAccountController() {
        AccountServiceController accountServiceController = mock(AccountServiceController.class);
        when(accountServiceController
                .update(eq("userId"), anyString(), any(se.tink.backend.rpc.UpdateAccountRequest.class)))
                .thenThrow(new NoSuchElementException());
        when(accountServiceController
                .update(eq("userId"), eq("accountId"), any(se.tink.backend.rpc.UpdateAccountRequest.class)))
                .thenReturn(backendAccount("accountId", 1));
        when(accountServiceController.list(eq("userId")))
                .thenReturn(Arrays.asList(backendAccount("id1", 1), backendAccount("id2", 2)));

        return accountServiceController;
    }

    private DefaultAuthenticationContext mockAuthenticationContext() {
        DefaultAuthenticationContext authenticationContext = mock(DefaultAuthenticationContext.class);
        when(authenticationContext.getUser()).thenReturn(new User() {{
            setId("userId");
            setProfile(new UserProfile() {{
                setCurrency("SEK");
            }});
        }});

        return authenticationContext;
    }

    @After
    public void tearDown() {
        inProcessServer.shutdown();
        inProcessChannel.shutdown();
    }

    @Test
    public void updateExistedAccount() {
        String accountId = "accountId";

        UpdateAccountResponse accountResponse = accountService.updateAccount(
                UpdateAccountRequest.newBuilder().setAccountId(accountId).setType(Account.Type.TYPE_CREDIT_CARD)
                        .build());
        assertNotNull(accountResponse.getAccount());
        assertEquals(accountId, accountResponse.getAccount().getId());
    }

    @Test
    public void errorOnNonExistedAccount() {
        thrown.expect(StatusRuntimeException.class);
        thrown.expectMessage("NOT_FOUND");

        UpdateAccountRequest request = UpdateAccountRequest.newBuilder().setAccountId("accountId2")
                .setType(Account.Type.TYPE_SAVINGS)
                .build();

        accountService.updateAccount(request);
    }

    @Test
    public void listAccounts() {
        ListAccountsResponse listResponse = accountService
                .listAccounts(ListAccountsRequest.getDefaultInstance());
        assertTrue(listResponse.isInitialized());
        assertEquals(2, listResponse.getAccountsCount());
    }

    private se.tink.backend.core.Account backendAccount(String id, double balance) {
        se.tink.backend.core.Account account = new se.tink.backend.core.Account();
        account.setId(id);
        account.setBalance(balance);
        account.setType(AccountTypes.CREDIT_CARD);
        account.setAccountNumber("123");
        account.setName("name");
        account.setOwnership(1.);
        account.setCredentialsId("credId");

        return account;
    }
}
