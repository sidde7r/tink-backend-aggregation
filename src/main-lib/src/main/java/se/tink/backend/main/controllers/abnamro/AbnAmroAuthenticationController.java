package se.tink.backend.main.controllers.abnamro;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.common.dao.AuthenticationTokenDao;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.User;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.abnamro.AbnAmroAuthenticationCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.client.IBClient;
import se.tink.libraries.abnamro.client.exceptions.InternetBankingUnavailableException;
import se.tink.libraries.abnamro.client.exceptions.UnauthorizedAccessException;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.metrics.MetricRegistry;

public class AbnAmroAuthenticationController {
    private static final LogUtils log = new LogUtils(AbnAmroAuthenticationController.class);
    private static final String ABNAMRO_MARKET = "NL";

    private final UserRepository userRepository;
    private final AuthenticationTokenDao authenticationDao;
    private final AbnAmroSubscriptionRepository abnAmroSubscriptionRepository;
    private final IBClient ibClient;

    @Inject
    public AbnAmroAuthenticationController(AbnAmroConfiguration abnAmroConfiguration, MetricRegistry metricRegistry,
            UserRepository userRepository, AuthenticationTokenDao authenticationDao,
            AbnAmroSubscriptionRepository abnAmroSubscriptionRepository) {
        this.userRepository = userRepository;
        this.authenticationDao = authenticationDao;
        this.abnAmroSubscriptionRepository = abnAmroSubscriptionRepository;
        this.ibClient = new IBClient(abnAmroConfiguration, metricRegistry);
    }

    /**
     * Authenticate a user towards Internet Banking at ABN AMRO. A typical flow is that the user first authenticate
     * with account number and PIN5 in a mobile client. The user will receive a session token which is sent to this
     * method. This method uses the session token to retrieve the customer number (bcNumber) which is used as the
     * username of the user.
     */
    public AuthenticationResponse authenticate(AbnAmroAuthenticationCommand command) {
        AuthenticationToken.AuthenticationTokenBuilder builder = AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.ABN_AMRO_PIN5)
                .withClientKey(command.getClientKey())
                .withMarket(ABNAMRO_MARKET)
                .withOAuth2ClientId(command.getOauthClientId());

        Optional<String> customerNumber = Optional.empty();
        try {
            customerNumber = ibClient.getCustomerNumber(command.getInternetBankingSessionToken());

            if (customerNumber.isPresent()) {
                User user = userRepository.findOneByUsername(AbnAmroLegacyUserUtils.getUsername(customerNumber.get()));

                if (user != null && hasActiveSubscription(user)) {
                    builder.withUserId(user.getId());
                    builder.withStatus(AuthenticationStatus.AUTHENTICATED);
                } else {
                    builder.withStatus(AuthenticationStatus.NO_USER);
                }
            } else {
                // A session should always be connected to a customer so this should not happen
                builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
            }

        } catch (InternetBankingUnavailableException e) {
            log.error("Internet Banking is unavailable.", e);
            builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
        } catch (UnauthorizedAccessException e) {
            log.error("Unauthorized access.", e);
            builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
        }

        AuthenticationToken token = authenticationDao.save(builder.build());

        log.debug(String.format("Authentication completed (Status = '%s', CustomerNumber = '%s', UserId = '%s').",
                token.getStatus(), customerNumber.orElse(null), token.getUserId()));

        AuthenticationResponse response = new AuthenticationResponse();
        response.setStatus(token.getStatus());
        response.setAuthenticationToken(token.getToken());

        return response;
    }

    /**
     * This will check if the user has an active subscription against ABN AMRO. The reason to why this check is needed
     * is because some users (i.e. business accounts and users below 16 years old) exist in the user table but they
     * don't have an active account.
     */
    private boolean hasActiveSubscription(User user) {
        AbnAmroSubscription subscription = abnAmroSubscriptionRepository.findOneByUserId(user.getId());

        return subscription != null && subscription.isActivated();
    }
}
