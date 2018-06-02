package se.tink.backend.grpc.v1.transports.authentication;

import com.google.inject.Inject;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.grpc.v1.converter.authentication.PhoneNumberAndPin6AuthenticationResponseConverter;
import se.tink.backend.grpc.v1.converter.authentication.ResetPin6ResponseConverter;
import se.tink.backend.grpc.v1.converter.authentication.sms.SmsOtpAndPin6ResponseConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.interceptors.RequestHeadersInterceptor;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.controllers.PhoneNumberAndPin6AuthenticationServiceController;
import se.tink.backend.main.controllers.exceptions.InvalidSmsOtpStatusException;
import se.tink.backend.main.controllers.exceptions.UserNotFoundException;
import se.tink.backend.rpc.UpdatePin6Command;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.PhoneNumberAndPin6AuthenticationCommand;
import se.tink.backend.rpc.auth.ResetPin6Command;
import se.tink.backend.rpc.auth.SmsOtpAndPin6AuthenticationCommand;
import se.tink.backend.rpc.auth.UpdatePhoneNumberCommand;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpNotFoundException;
import se.tink.grpc.v1.rpc.PhoneNumberAndPin6AuthenticationRequest;
import se.tink.grpc.v1.rpc.PhoneNumberAndPin6AuthenticationResponse;
import se.tink.grpc.v1.rpc.ResetPin6Request;
import se.tink.grpc.v1.rpc.ResetPin6Response;
import se.tink.grpc.v1.rpc.SmsOtpAndPin6AuthenticationRequest;
import se.tink.grpc.v1.rpc.SmsOtpAndPin6AuthenticationResponse;
import se.tink.grpc.v1.rpc.UpdatePhoneNumberRequest;
import se.tink.grpc.v1.rpc.UpdatePhoneNumberResponse;
import se.tink.grpc.v1.rpc.UpdatePin6Request;
import se.tink.grpc.v1.rpc.UpdatePin6Response;
import se.tink.grpc.v1.services.PhoneNumberAndPin6AuthenticationServiceGrpc;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class PhoneNumberAndPin6AuthenticationGrpcTransport
        extends PhoneNumberAndPin6AuthenticationServiceGrpc.PhoneNumberAndPin6AuthenticationServiceImplBase {

    private final PhoneNumberAndPin6AuthenticationServiceController serviceController;

    @Inject
    public PhoneNumberAndPin6AuthenticationGrpcTransport(
            PhoneNumberAndPin6AuthenticationServiceController serviceController) {
        this.serviceController = serviceController;
    }

    @Override
    @Authenticated(required = false)
    public void smsOtpAndPin6Authentication(SmsOtpAndPin6AuthenticationRequest request,
            StreamObserver<SmsOtpAndPin6AuthenticationResponse> observer) {
        Metadata headers = RequestHeadersInterceptor.HEADERS.get();

        try {
            SmsOtpAndPin6AuthenticationCommand command = new SmsOtpAndPin6AuthenticationCommand(
                    request.getSmsOtpVerificationToken(),
                    request.getPin6(),
                    headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME),
                    headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME),
                    request.getMarketCode(),
                    headers.get(TinkGrpcHeaders.DEVICE_ID_HEADER_NAME),
                    AuthenticationInterceptor.CONTEXT.get().getRemoteAddress().orElse(null));

            AuthenticationResponse response = serviceController.smsOtpAndPin6Authentication(command);

            observer.onNext(new SmsOtpAndPin6ResponseConverter().convertFrom(response));
            observer.onCompleted();
        } catch (InvalidPin6Exception e) {
            throw ApiError.Validation.INVALID_PIN_6.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false)
    public void phoneNumberAndPin6Authentication(PhoneNumberAndPin6AuthenticationRequest request,
            StreamObserver<PhoneNumberAndPin6AuthenticationResponse> observer) {
        Metadata headers = RequestHeadersInterceptor.HEADERS.get();
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            PhoneNumberAndPin6AuthenticationCommand command = PhoneNumberAndPin6AuthenticationCommand.builder()
                    .withPhoneNumber(request.getPhoneNumber())
                    .withPin6(request.getPin6())
                    .withMarket(request.getMarketCode())
                    .withClientKey(headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME))
                    .withOauthClientId(headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME))
                    .withRemoteAddress(authenticationContext.getRemoteAddress())
                    .withUserAgent(authenticationContext.getUserAgent().orElse(null))
                    .withUserDeviceId(authenticationContext.getUserDeviceId().orElse(null))
                    .build();

            AuthenticationResponse response = serviceController.phoneNumberAndPin6Authentication(command);

            observer.onNext(new PhoneNumberAndPin6AuthenticationResponseConverter().convertFrom(response));
            observer.onCompleted();
        } catch (InvalidPhoneNumberException e) {
            throw ApiError.Validation.INVALID_PHONE_NUMBER.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void updatePin6(UpdatePin6Request request, StreamObserver<UpdatePin6Response> observer) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            UpdatePin6Command command = UpdatePin6Command.builder()
                    .withOldPin6(request.getOldPin6())
                    .withNewPin6(request.getNewPin6())
                    .withSessionId(authenticationContext.getAuthenticationDetails().getSessionId().orElse(null))
                    .withRemoteAddress(authenticationContext.getRemoteAddress())
                    .build();

            serviceController.updatePin6(authenticationContext.getUser(), command);
            observer.onNext(UpdatePin6Response.getDefaultInstance());
            observer.onCompleted();
        } catch (InvalidPin6Exception e) {
            throw ApiError.Validation.INVALID_PIN_6.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void updatePhoneNumber(UpdatePhoneNumberRequest request,
            StreamObserver<UpdatePhoneNumberResponse> observer) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            UpdatePhoneNumberCommand command = UpdatePhoneNumberCommand.builder()
                    .withSmsOtpVerificationToken(request.getSmsOtpVerificationToken())
                    .withPin6(request.getPin6())
                    .withUser(authenticationContext.getUser())
                    .withSessionId(authenticationContext.getAuthenticationDetails().getSessionId())
                    .withRemoteAddress(authenticationContext.getRemoteAddress())
                    .build();

            serviceController.updatePhoneNumber(command);

            observer.onNext(UpdatePhoneNumberResponse.getDefaultInstance());
            observer.onCompleted();
        } catch (SmsOtpNotFoundException e) {
            throw ApiError.Authentication.SmsOtp.NOT_FOUND.withCause(e).exception();
        } catch (InvalidPhoneNumberException e) {
            throw ApiError.Validation.INVALID_PHONE_NUMBER.withCause(e).exception();
        } catch (InvalidPin6Exception e) {
            throw ApiError.Validation.INVALID_PIN_6.withCause(e).exception();
        } catch (InvalidSmsOtpStatusException e) {
            throw ApiError.Authentication.SmsOtp.INVALID_STATUS.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(required = false)
    public void resetPin6(ResetPin6Request request, StreamObserver<ResetPin6Response> observer) {
        Metadata headers = RequestHeadersInterceptor.HEADERS.get();

        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            ResetPin6Command command = ResetPin6Command.builder()
                    .withSmsOtpVerificationToken(request.getSmsOtpVerificationToken())
                    .withPin6(request.getPin6())
                    .withRemoteAddress(authenticationContext.getRemoteAddress().orElse(null))
                    .withClientKey(headers.get(TinkGrpcHeaders.CLIENT_KEY_HEADER_NAME))
                    .withOauthClientId(headers.get(TinkGrpcHeaders.OAUTH_CLIENT_ID_HEADER_NAME))
                    .withUserAgent(authenticationContext.getUserAgent().orElse(null))
                    .withUserDeviceId(authenticationContext.getUserDeviceId().orElse(null))
                    .build();

            AuthenticationResponse response = serviceController.resetPin6(command);

            observer.onNext(new ResetPin6ResponseConverter().convertFrom(response));
            observer.onCompleted();
        } catch (SmsOtpNotFoundException e) {
            throw ApiError.Authentication.SmsOtp.NOT_FOUND.withCause(e).exception();
        } catch (UserNotFoundException e) {
            throw ApiError.Users.NOT_FOUND.withCause(e).exception();
        } catch (InvalidSmsOtpStatusException e) {
            throw ApiError.Authentication.SmsOtp.INVALID_STATUS.withCause(e).exception();
        } catch (InvalidPin6Exception e) {
            throw ApiError.Validation.INVALID_PIN_6.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        }
    }
}
