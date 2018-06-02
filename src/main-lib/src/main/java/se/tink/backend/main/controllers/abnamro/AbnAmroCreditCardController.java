package se.tink.backend.main.controllers.abnamro;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;

public class AbnAmroCreditCardController {

    private static final LogUtils log = new LogUtils(AbnAmroCreditCardController.class);

    private final boolean isAbnAmroCluster;
    private final CredentialsRepository credentialsRepository;
    private final AbnAmroAccountController abnAmroAccountController;
    private final CredentialsRequestRunnableFactory credentialsRequestRunnableFactory;
    private final Provider<ServiceContext> serviceContextProvider;
    private final AbnAmroConfiguration abnAmroConfiguration;

    @Inject
    public AbnAmroCreditCardController(Cluster cluster, CredentialsRepository credentialsRepository,
            CredentialsRequestRunnableFactory credentialsRequestRunnableFactory,
            AbnAmroAccountController abnAmroAccountController, Provider<ServiceContext> serviceContextProvider,
            AbnAmroConfiguration abnAmroConfiguration) {

        this.isAbnAmroCluster = Objects.equals(cluster, Cluster.ABNAMRO);
        this.credentialsRepository = credentialsRepository;
        this.credentialsRequestRunnableFactory = credentialsRequestRunnableFactory;
        this.abnAmroAccountController = abnAmroAccountController;
        this.serviceContextProvider = serviceContextProvider;
        this.abnAmroConfiguration = abnAmroConfiguration;
    }

    /**
     * Create an ICS (Credit Card) credential.
     * - A credential will not be created if the user does't have any credit cards.
     * - We are only (from legal reasons) allowed to collect data from the accounts that the user has enabled.
     */
    public Optional<Credentials> updateCredentials(AuthenticatedUser authenticatedUser,
            List<Account> creditCardAccounts) {

        Preconditions.checkState(isAbnAmroCluster);

        User user = authenticatedUser.getUser();

        if (CollectionUtils.isEmpty(creditCardAccounts)) {
            log.info(user.getId(), "No credit cards available. No ICS credentials created.");
            return Optional.empty();
        }

        Optional<Credentials> credentials = getExistingCredentials(user);

        if (credentials.isPresent()) {
            // Credentials already exist, update and refresh
            credentials = Optional.of(update(credentials.get(), creditCardAccounts));

            refreshCredential(user, credentials.get());
        } else {
            // Credential will be created and automatically refreshed
            credentials = Optional.of(create(authenticatedUser, creditCardAccounts));
        }

        abnAmroAccountController.updateAccounts(credentials.get(), creditCardAccounts);

        return credentials;
    }

    private Credentials update(Credentials credentials, List<Account> creditCardAccounts) {
        AbnAmroIcsCredentials icsCredentials = new AbnAmroIcsCredentials(credentials);

        icsCredentials.addContractNumbers(creditCardAccounts);

        return credentialsRepository.save(icsCredentials.getCredentials());
    }

    private Credentials create(AuthenticatedUser authenticatedUser, List<Account> creditCardAccounts) {
        AbnAmroIcsCredentials abnAmroIcsCredentials = AbnAmroIcsCredentials
                .create(authenticatedUser.getUser(), creditCardAccounts);

        CredentialsService service = serviceContextProvider.get().getServiceFactory().getCredentialsService();

        return service.create(authenticatedUser, null, abnAmroIcsCredentials.getCredentials(),
                Collections.emptySet());
    }

    /**
     * Refresh a a credit card credential if it isn't in the status `DISABLED`.
     */
    private void refreshCredential(User user, Credentials credentials) {

        Preconditions.checkState(isAbnAmroCluster);
        Preconditions.checkNotNull(credentials, "Credentials can not be null.");

        if (Objects.equals(CredentialsStatus.DISABLED, credentials.getStatus())) {
            return;
        }

        serviceContextProvider.get().execute(
                credentialsRequestRunnableFactory.createRefreshRunnable(user, credentials, false, true, false));
    }

    private Optional<Credentials> getExistingCredentials(User user) {

        List<Credentials> existingCredentials = credentialsRepository.findAllByUserIdAndProviderName(user.getId(),
                AbnAmroIcsCredentials.ABN_AMRO_ICS_PROVIDER_NAME);

        Preconditions.checkState(existingCredentials.size() <= 1);

        return existingCredentials.stream().findFirst();
    }

    /**
     * There is a special logic for ABN AMRO credit cards. If the  include an excluded credit card we need to add the
     * account number to the ICS credential and trigger a refresh of the credential. The opposite is not needed, so we
     * don't have to do anything if the account is excluded.
     */
    public void accountUpdated(User user, Account account) {

        Preconditions.checkState(isAbnAmroCluster);

        // No need to check anything if the user has excluded the account
        if (account.isExcluded()) {
            return;
        }

        Credentials credentials = credentialsRepository.findOne(account.getCredentialsId());

        // Only valid for ICS credit cards
        if (!AbnAmroIcsCredentials.isAbnAmroIcsCredentials(credentials)) {
            return;
        }

        AbnAmroIcsCredentials abnAmroIcsCredentials = new AbnAmroIcsCredentials(credentials);

        // Add the new contract number and refresh the credentials
        abnAmroIcsCredentials.addContractNumber(Long.parseLong(account.getBankId()));

        credentials = abnAmroIcsCredentials.getCredentials();

        credentialsRepository.save(credentials);

        serviceContextProvider.get()
                .execute(credentialsRequestRunnableFactory.createRefreshRunnable(user, credentials, true, false, true));
    }
}
