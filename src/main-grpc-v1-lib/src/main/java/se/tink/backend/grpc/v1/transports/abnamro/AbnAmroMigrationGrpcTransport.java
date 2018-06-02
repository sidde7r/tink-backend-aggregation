package se.tink.backend.grpc.v1.transports.abnamro;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.streaming.StreamingQueueConsumerHandler;
import se.tink.backend.grpc.v1.streaming.context.ContextGenerator;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.main.controllers.abnamro.AbnAmroMigrationController;
import se.tink.backend.main.controllers.abnamro.exceptions.PhoneNumberAlreadyInUseException;
import se.tink.backend.main.controllers.abnamro.exceptions.UserAlreadyMigratedException;
import se.tink.backend.main.controllers.exceptions.InvalidSmsOtpStatusException;
import se.tink.backend.rpc.abnamro.AbnAmroMigrationCommand;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpNotFoundException;
import se.tink.grpc.v1.rpc.AbnAmroMigrationRequest;
import se.tink.grpc.v1.rpc.AbnAmroMigrationResponse;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.grpc.v1.services.AbnAmroMigrationServiceGrpc;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class AbnAmroMigrationGrpcTransport extends AbnAmroMigrationServiceGrpc.AbnAmroMigrationServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(AbnAmroMigrationGrpcTransport.class);

    private final AbnAmroMigrationController migrationController;
    private final StreamingQueueConsumerHandler queueConsumerHandler;
    private final ContextGenerator contextGenerator;
    private final CredentialServiceController credentialServiceController;

    @Inject
    public AbnAmroMigrationGrpcTransport(AbnAmroMigrationController abnAmroMigrationController,
            StreamingQueueConsumerHandler queueConsumerHandler, ContextGenerator contextGenerator,
            CredentialServiceController credentialServiceController) {
        this.migrationController = abnAmroMigrationController;
        this.queueConsumerHandler = queueConsumerHandler;
        this.contextGenerator = contextGenerator;
        this.credentialServiceController = credentialServiceController;
    }

    @Override
    @Authenticated(requireAuthorizedDevice = false)
    public void migrate(AbnAmroMigrationRequest request, StreamObserver<AbnAmroMigrationResponse> observer) {
        DefaultAuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            AbnAmroMigrationCommand command = AbnAmroMigrationCommand.builder()
                    .withUser(authenticationContext.getUser())
                    .withPin6(request.getPin6())
                    .withRemoteAddress(authenticationContext.getRemoteAddress())
                    .withSmsOtpVerificationToken(request.getSmsOtpVerificationToken())
                    .withUserDeviceId(authenticationContext.getUserDeviceId().orElse(null))
                    .withUserAgent(authenticationContext.getUserAgent().orElse(null))
                    .build();

            migrationController.migrate(command);

            // The migration moves, remove and merges accounts and transactions without sending them on the firehose
            // so we want to give the clients a fresh context to work with.
            generateAndSendContext(authenticationContext.getUser());

            observer.onNext(AbnAmroMigrationResponse.getDefaultInstance());
            observer.onCompleted();
        } catch (InvalidPin6Exception e) {
            throw ApiError.Validation.INVALID_PIN_6.withCause(e).exception();
        } catch (InvalidSmsOtpStatusException e) {
            throw ApiError.Authentication.SmsOtp.INVALID_STATUS.withCause(e).exception();
        } catch (InvalidPhoneNumberException e) {
            throw ApiError.Validation.INVALID_PHONE_NUMBER.withCause(e).exception();
        } catch (SmsOtpNotFoundException e) {
            throw ApiError.Authentication.SmsOtp.NOT_FOUND.withCause(e).exception();
        } catch (PhoneNumberAlreadyInUseException e) {
            throw ApiError.Authentication.USER_ALREADY_REGISTERED.withWarnSeverity().withCause(e).exception();
        } catch (UserAlreadyMigratedException e) {
            throw ApiError.AbnAmroMigration.ALREADY_MIGRATED.withCause(e).exception();
        }
    }

    /**
     * Generate and send a new context to all streams for an user.
     */
    private void generateAndSendContext(User user) {
        Map<String, Provider> providersByCredentialIds = credentialServiceController
                .getProvidersByCredentialIds(user.getId());

        try {
            StreamingResponse context = contextGenerator.generateContext(user, providersByCredentialIds);
            queueConsumerHandler.sendContext(user.getId(), context);
        } catch (LockException e) {
            // We don't want the whole migration to fail if the context couldn't be generated. Just log the error and
            // then return.
            log.error("Could not send context to clients.", e);
        }
    }
}
