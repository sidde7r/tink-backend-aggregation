package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.consent.controllers.ConsentServiceController;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.exceptions.ConsentNotFoundException;
import se.tink.backend.consent.core.exceptions.ConsentRequestInvalid;
import se.tink.backend.consent.core.exceptions.InvalidChecksumException;
import se.tink.backend.consent.core.exceptions.UserConsentNotFoundException;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.consent.CoreConsentRequestConverter;
import se.tink.backend.grpc.v1.converter.consent.GrpcConsentConverter;
import se.tink.backend.grpc.v1.converter.consent.GrpcUserConsentConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.grpc.v1.rpc.AvailableConsentsRequest;
import se.tink.grpc.v1.rpc.AvailableConsentsResponse;
import se.tink.grpc.v1.rpc.ConsentDetailsRequest;
import se.tink.grpc.v1.rpc.ConsentDetailsResponse;
import se.tink.grpc.v1.rpc.GiveConsentRequest;
import se.tink.grpc.v1.rpc.GiveConsentResponse;
import se.tink.grpc.v1.rpc.UserConsentDetailsRequest;
import se.tink.grpc.v1.rpc.UserConsentDetailsResponse;
import se.tink.grpc.v1.rpc.UserConsentsListRequest;
import se.tink.grpc.v1.rpc.UserConsentsListResponse;
import se.tink.grpc.v1.services.ConsentServiceGrpc;

public class ConsentGrpcTransport extends ConsentServiceGrpc.ConsentServiceImplBase {
    private static final ModelMapper mapper = new ModelMapper();

    private final ConsentServiceController consentServiceController;

    static {
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.createTypeMap(User.class, se.tink.backend.consent.core.User.class);
    }

    private static se.tink.backend.consent.core.User map(User user) {
        return mapper.map(user, se.tink.backend.consent.core.User.class);
    }

    @Inject
    public ConsentGrpcTransport(ConsentServiceController consentServiceController) {
        this.consentServiceController = consentServiceController;
    }

    @Override
    @Authenticated
    public void availableConsents(AvailableConsentsRequest request, StreamObserver<AvailableConsentsResponse> observer) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        List<Consent> consents = consentServiceController.available(map(user));

        AvailableConsentsResponse response = AvailableConsentsResponse.newBuilder()
                .addAllConsents(GrpcConsentConverter.convert(consents))
                .build();

        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    @Authenticated
    public void listUserConsents(UserConsentsListRequest request, StreamObserver<UserConsentsListResponse> observer) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        List<UserConsent> userConsents = consentServiceController.list(map(user));

        UserConsentsListResponse response = UserConsentsListResponse.newBuilder()
                .addAllUserConsents(GrpcUserConsentConverter.convert(userConsents))
                .build();

        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    @Authenticated
    public void giveConsent(GiveConsentRequest request, StreamObserver<GiveConsentResponse> observer) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        try {
            UserConsent userConsent = consentServiceController
                    .consent(map(user), CoreConsentRequestConverter.convert(request));

            GiveConsentResponse response = GiveConsentResponse.newBuilder()
                    .setUserConsent(GrpcUserConsentConverter.convert(userConsent))
                    .build();

            observer.onNext(response);
            observer.onCompleted();
        } catch (ConsentNotFoundException e) {
            throw ApiError.Consents.NOT_FOUND.withCause(e).exception();
        } catch (InvalidChecksumException e) {
            throw ApiError.Consents.INVALID_CHECKSUM.withCause(e).exception();
        } catch (ConsentRequestInvalid e) {
            throw ApiError.Consents.INVALID_REQUEST.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void userConsentDetails(UserConsentDetailsRequest request,
            StreamObserver<UserConsentDetailsResponse> observer) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        try {
            UserConsent userConsent = consentServiceController.details(map(user), request.getId());

            UserConsentDetailsResponse response = UserConsentDetailsResponse.newBuilder()
                    .setUserConsent(GrpcUserConsentConverter.convert(userConsent))
                    .build();

            observer.onNext(response);
            observer.onCompleted();
        } catch (UserConsentNotFoundException e) {
            throw ApiError.Consents.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void consentDetails(ConsentDetailsRequest request, StreamObserver<ConsentDetailsResponse> observer) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        try {
            Consent consent = consentServiceController.describe(map(user), request.getKey());

            ConsentDetailsResponse response = ConsentDetailsResponse.newBuilder()
                    .setConsent(GrpcConsentConverter.convert(consent))
                    .build();

            observer.onNext(response);
            observer.onCompleted();
        } catch (ConsentNotFoundException e) {
            throw ApiError.Consents.NOT_FOUND.withCause(e).exception();
        }
    }

    public static ModelMapper getModelMapper() {
        return mapper;
    }
}
