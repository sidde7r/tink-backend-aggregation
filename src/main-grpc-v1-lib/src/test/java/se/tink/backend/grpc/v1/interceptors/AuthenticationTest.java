package se.tink.backend.grpc.v1.interceptors;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.grpc.v1.auth.GrpcAuthenticationProvider;
import se.tink.backend.grpc.v1.guice.configuration.GrpcModule;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.grpc.v1.rpc.ListAccountsRequest;
import se.tink.grpc.v1.rpc.ListAccountsResponse;
import se.tink.grpc.v1.rpc.UpdateAccountRequest;
import se.tink.grpc.v1.rpc.UpdateAccountResponse;
import se.tink.grpc.v1.services.AccountServiceGrpc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthenticationTest {
    private Server inProcessServer;
    private ManagedChannel inProcessChannel;
    private AccountServiceGrpc.AccountServiceBlockingStub accountService;
    private GrpcAuthenticationProvider authenticationProvider;

    @Before
    public void setUp() throws IOException {
        Injector injector = Guice.createInjector(new TestInterceptorsModule());
        inProcessServer = injector.getInstance(Server.class);
        inProcessServer.start();
        inProcessChannel = injector.getInstance(ManagedChannel.class);
        authenticationProvider = injector.getInstance(GrpcAuthenticationProvider.class);
        accountService = AccountServiceGrpc.newBlockingStub(inProcessChannel);
    }

    @After
    public void tearDown() {
        inProcessChannel.shutdown();
        inProcessServer.shutdown();
    }

    @Test
    public void doAuthentication() throws IllegalAccessException {
        when(authenticationProvider.authenticate(any(Metadata.class), any(Authenticated.class), any(String.class)))
                .thenReturn(mock(DefaultAuthenticationContext.class));
        accountService.listAccounts(ListAccountsRequest.getDefaultInstance());
    }

    @Test
    public void throwAuthenticationException() throws IllegalAccessException {
        when(authenticationProvider.authenticate(any(Metadata.class), any(Authenticated.class), any(String.class)))
                .thenThrow(Status.UNAUTHENTICATED.asRuntimeException());
        try {
            accountService.listAccounts(ListAccountsRequest.getDefaultInstance());
            fail("Exception wasn't thrown");
        } catch (StatusRuntimeException ex) {
            assertEquals(Status.UNAUTHENTICATED.getCode(), ex.getStatus().getCode());
        }
    }

    @Test
    public void notAuthenticateWithoutAnnotation() throws IllegalAccessException {
        when(authenticationProvider.authenticate(any(Metadata.class), any(Authenticated.class), any(String.class)))
                .thenReturn(mock(DefaultAuthenticationContext.class));
        try {
            accountService.updateAccount(UpdateAccountRequest.getDefaultInstance());
        } catch (StatusRuntimeException ex) {
            assertEquals(Status.UNAUTHENTICATED.getCode(), ex.getStatus().getCode());
        }
        verify(authenticationProvider, never())
                .authenticate(any(Metadata.class), any(Authenticated.class), any(String.class));
    }

    public static class TestInterceptorsModule extends AbstractModule {
        private static final String SERVER_NAME = "in-process server for " + AuthenticationTest.class;

        @Override
        protected void configure() {
            bind(BindableService.class).to(AccountServiceGrpcTest.class);
            bind(GrpcAuthenticationProvider.class).toInstance(mock(GrpcAuthenticationProvider.class));
            GrpcModule.bindInterceptors(binder());
        }

        @Provides
        private Server provideInProcessServer(BindableService bindableService,
                @Named("grpcInterceptors") Set<ServerInterceptor> interceptors) {
            return InProcessServerBuilder.forName(SERVER_NAME)
                    .addService(ServerInterceptors.intercept(bindableService, Lists.newArrayList(interceptors)))
                    .directExecutor()
                    .build();
        }

        @Provides
        private ManagedChannel provideInProcessChannel() {
            return InProcessChannelBuilder.forName(SERVER_NAME).directExecutor().build();
        }
    }

    private static class AccountServiceGrpcTest extends AccountServiceGrpc.AccountServiceImplBase {
        @Override
        @Authenticated
        public void listAccounts(ListAccountsRequest request, StreamObserver<ListAccountsResponse> responseObserver) {
            if (AuthenticationInterceptor.CONTEXT.get() == null) {
                responseObserver
                        .onError(Status.UNKNOWN.withDescription("User was not authenticated").asRuntimeException());
                return;
            }
            responseObserver.onNext(ListAccountsResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void updateAccount(UpdateAccountRequest request,
                StreamObserver<UpdateAccountResponse> responseObserver) {
            if (AuthenticationInterceptor.CONTEXT.get() != null) {
                responseObserver.onError(
                        Status.UNKNOWN.withDescription("Authentication is not expected here").asRuntimeException());
                return;
            }
            responseObserver.onNext(UpdateAccountResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

}
