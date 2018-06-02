package se.tink.backend.grpc.v1.transports.authentication;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.authentication.sms.InitiateSmsOtpResponseConverter;
import se.tink.backend.grpc.v1.converter.authentication.sms.VerifySmsOtpResponseConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.controllers.PhoneNumberAuthenticationServiceController;
import se.tink.backend.rpc.auth.otp.InitiateSmsOtpCommand;
import se.tink.backend.rpc.auth.otp.VerifySmsOtpCommand;
import se.tink.backend.sms.otp.core.exceptions.PhoneNumberBlockedException;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpCouldNotBeSentException;
import se.tink.grpc.v1.rpc.InitiateSmsOtpRequest;
import se.tink.grpc.v1.rpc.InitiateSmsOtpResponse;
import se.tink.grpc.v1.rpc.VerifySmsOtpRequest;
import se.tink.grpc.v1.rpc.VerifySmsOtpResponse;
import se.tink.grpc.v1.services.PhoneNumberAuthenticationServiceGrpc;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

public class PhoneNumberAuthenticationGrpcTransport
        extends PhoneNumberAuthenticationServiceGrpc.PhoneNumberAuthenticationServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PhoneNumberAuthenticationGrpcTransport.class);

    private final PhoneNumberAuthenticationServiceController serviceController;

    @Inject
    public PhoneNumberAuthenticationGrpcTransport(PhoneNumberAuthenticationServiceController serviceController) {
        this.serviceController = serviceController;
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void initiateSmsOtp(InitiateSmsOtpRequest request, StreamObserver<InitiateSmsOtpResponse> observer) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        Optional<User> user = authenticationContext.isAuthenticated() ?
                Optional.ofNullable(authenticationContext.getUser()) : Optional.empty();

        try {
            InitiateSmsOtpCommand command = InitiateSmsOtpCommand.builder()
                    .withPhoneNumber(request.getPhoneNumber())
                    .withLocale(request.getLocale())
                    .withUser(user)
                    .withRemoteAddress(authenticationContext.getRemoteAddress())
                    .build();

            se.tink.backend.rpc.auth.otp.InitiateSmsOtpResponse response = serviceController.initiateSmsOtp(command);

            observer.onNext(new InitiateSmsOtpResponseConverter().convertFrom(response));
            observer.onCompleted();
        } catch (InvalidLocaleException e) {
            throw ApiError.Validation.INVALID_LOCALE.exception();
        } catch (InvalidPhoneNumberException e) {
            throw ApiError.Validation.INVALID_PHONE_NUMBER.withCause(e).exception();
        } catch (SmsOtpCouldNotBeSentException e) {
            throw ApiError.Authentication.SmsOtp.GATEWAY_UNAVAILABLE.exception();
        } catch (PhoneNumberBlockedException e) {
            throw ApiError.Authentication.SmsOtp.PHONE_NUMBER_BLOCKED.withWarnSeverity().exception();
        }
    }

    @Override
    @Authenticated(required = false, requireAuthorizedDevice = false)
    public void verifySmsOtp(VerifySmsOtpRequest request, StreamObserver<VerifySmsOtpResponse> streamObserver) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        Optional<User> user = authenticationContext.isAuthenticated() ?
                Optional.ofNullable(authenticationContext.getUser()) : Optional.empty();

        VerifySmsOtpCommand verifyCommand = VerifySmsOtpCommand.builder()
                .withCode(request.getCode())
                .withSmsOtpVerificationToken(request.getSmsOtpVerificationToken())
                .withUser(user)
                .withRemoteAddress(authenticationContext.getRemoteAddress())
                .build();

        se.tink.backend.rpc.auth.otp.VerifySmsOtpResponse verifyResponse = serviceController
                .verifySmsOtp(verifyCommand);

        VerifySmsOtpResponse response = new VerifySmsOtpResponseConverter().convertFrom(verifyResponse);

        switch (verifyResponse.getResult()) {
        case CORRECT_CODE:
            log.debug("Sms otp verification succeeded.");
            streamObserver.onNext(response);
            streamObserver.onCompleted();
            break;
        case INCORRECT_CODE:
            log.warn("Sms otp verification failed. Code is incorrect.");
            streamObserver.onNext(response);
            streamObserver.onCompleted();
            break;
        case OTP_EXPIRED:
            log.error("Sms otp verification failed. Sms otp expired.");
            streamObserver.onNext(response);
            streamObserver.onCompleted();
            break;
        case TOO_MANY_VERIFICATION_ATTEMPTS:
            log.error("Sms otp verification failed. Exceeded number of verification attempts.");
            streamObserver.onNext(response);
            streamObserver.onCompleted();
            break;
        case OTP_NOT_FOUND:
            throw ApiError.Authentication.SmsOtp.NOT_FOUND.exception();
        case INVALID_OTP_STATUS:
            throw ApiError.Authentication.SmsOtp.INVALID_STATUS.exception();
        }
    }
}
