package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.Locale;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.exceptions.InitializationException;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.SupplementalStatus;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.credential.CoreCredentialToGrpcCredentialConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.rpc.credentials.SupplementalInformationCommand;
import se.tink.grpc.v1.rpc.CancelSupplementInformationRequest;
import se.tink.grpc.v1.rpc.CancelSupplementInformationResponse;
import se.tink.grpc.v1.rpc.CreateCredentialRequest;
import se.tink.grpc.v1.rpc.CreateCredentialResponse;
import se.tink.grpc.v1.rpc.DeleteCredentialRequest;
import se.tink.grpc.v1.rpc.DeleteCredentialResponse;
import se.tink.grpc.v1.rpc.DisableCredentialRequest;
import se.tink.grpc.v1.rpc.DisableCredentialResponse;
import se.tink.grpc.v1.rpc.EnableCredentialRequest;
import se.tink.grpc.v1.rpc.EnableCredentialResponse;
import se.tink.grpc.v1.rpc.ListCredentialsRequest;
import se.tink.grpc.v1.rpc.ListCredentialsResponse;
import se.tink.grpc.v1.rpc.RefreshCredentialsRequest;
import se.tink.grpc.v1.rpc.RefreshCredentialsResponse;
import se.tink.grpc.v1.rpc.SupplementInformationRequest;
import se.tink.grpc.v1.rpc.SupplementInformationResponse;
import se.tink.grpc.v1.rpc.UpdateCredentialRequest;
import se.tink.grpc.v1.rpc.UpdateCredentialResponse;
import se.tink.grpc.v1.services.CredentialServiceGrpc;

public class CredentialGrpcTransport extends CredentialServiceGrpc.CredentialServiceImplBase {
    private final CredentialServiceController credentialServiceController;

    @Inject
    public CredentialGrpcTransport(CredentialServiceController credentialServiceController) {
        this.credentialServiceController = credentialServiceController;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE)
    public void createCredential(CreateCredentialRequest createCredentialRequest,
            StreamObserver<CreateCredentialResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            Locale locale = new Locale(authenticationContext.getUser().getLocale());

            Credentials credentials = credentialServiceController
                    .create(authenticationContext, createCredentialRequest.getProviderName(),
                            EnumMappers.CORE_CREDENTIALS_TYPE_TO_GRPC_MAP.inverse()
                                    .get(createCredentialRequest.getType()),
                            createCredentialRequest.getFieldsMap());
            streamObserver.onNext(CreateCredentialResponse.newBuilder()
                    .setCredential(new CoreCredentialToGrpcCredentialConverter(locale).convertFrom(credentials))
                    .build());
            streamObserver.onCompleted();
        } catch (DuplicateException e) {
            throw ApiError.Credentials.ALREADY_EXIST.withWarnSeverity().withCause(e).exception();
        } catch (IllegalAccessException e) {
            throw ApiError.Credentials.PERMISSION_DENIED.withCause(e).exception();
        } catch (InitializationException e) {
            throw ApiError.Credentials.UNKNOWN_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE)
    public void updateCredential(UpdateCredentialRequest updateCredentialRequest,
            StreamObserver<UpdateCredentialResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        Locale locale = new Locale(authenticationContext.getUser().getLocale());

        Credentials credentials = credentialServiceController
                .update(authenticationContext, updateCredentialRequest.getCredentialId(),
                        updateCredentialRequest.getFieldsMap());
        streamObserver.onNext(UpdateCredentialResponse.newBuilder()
                .setCredential(new CoreCredentialToGrpcCredentialConverter(locale).convertFrom(credentials))
                .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_READ)
    public void listCredentials(ListCredentialsRequest request, StreamObserver<ListCredentialsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        Locale locale = new Locale(authenticationContext.getUser().getLocale());

        streamObserver.onNext(ListCredentialsResponse.newBuilder()
                .addAllCredentials(new CoreCredentialToGrpcCredentialConverter(locale)
                        .convertFrom(credentialServiceController.list(authenticationContext.getUser())))
                .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE)
    public void deleteCredential(DeleteCredentialRequest deleteCredentialRequest,
            StreamObserver<DeleteCredentialResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        credentialServiceController.delete(authenticationContext.getUser(), deleteCredentialRequest.getCredentialId(),
                authenticationContext.getRemoteAddress());
        streamObserver.onNext(DeleteCredentialResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH)
    public void refreshCredentials(RefreshCredentialsRequest refreshCredentialsRequest,
            StreamObserver<RefreshCredentialsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        credentialServiceController.refresh(authenticationContext.getUser(),
                Sets.newHashSet(refreshCredentialsRequest.getCredentialIdsList()));
        streamObserver.onNext(RefreshCredentialsResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH)
    public void supplementInformation(SupplementInformationRequest supplementInformationRequest,
            StreamObserver<SupplementInformationResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId(authenticationContext.getUser().getId())
                .withCredentialsId(supplementInformationRequest.getCredentialId())
                .withSupplementalInformation(supplementInformationRequest.getSupplementalInformationFieldsMap())
                .withStatus(SupplementalStatus.OK)
                .build();

        credentialServiceController.supplement(command);
        streamObserver.onNext(SupplementInformationResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH)
    public void cancelSupplementInformation(CancelSupplementInformationRequest request,
            StreamObserver<CancelSupplementInformationResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId(authenticationContext.getUser().getId())
                .withCredentialsId(request.getCredentialId())
                .withStatus(SupplementalStatus.CANCELLED)
                .build();

        credentialServiceController.supplement(command);
        streamObserver.onNext(CancelSupplementInformationResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }
    
    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE)
    public void enableCredential(EnableCredentialRequest enableCredentialRequest,
            StreamObserver<EnableCredentialResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        credentialServiceController
                .enable(authenticationContext.getUser(), enableCredentialRequest.getCredentialId());
        streamObserver.onNext(EnableCredentialResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE)
    public void disableCredential(DisableCredentialRequest disableCredentialRequest,
            StreamObserver<DisableCredentialResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        credentialServiceController
                .disable(authenticationContext.getUser(), disableCredentialRequest.getCredentialId());
        streamObserver.onNext(DisableCredentialResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }
}
