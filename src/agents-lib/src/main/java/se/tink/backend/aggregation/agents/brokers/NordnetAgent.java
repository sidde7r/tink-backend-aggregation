package se.tink.backend.aggregation.agents.brokers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.PositionEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.PositionsResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.AccountResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;

public class NordnetAgent extends AbstractAgent implements RefreshableItemExecutor {
    private static final int MAX_ATTEMPTS = 60;

    private final Credentials credentials;
    private final NordnetApiClient apiClient;

    // cache
    private AccountResponse accounts = null;

    public NordnetAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        apiClient = new NordnetApiClient(clientFactory.createClientWithRedirectHandler(context.getLogOutputStream(),
                NordnetApiClient.REDIRECT_STRATEGY), DEFAULT_USER_AGENT);
    }

    private void refreshSavingsAccounts(AccountResponse accountEntities) {
        accountEntities.forEach(accountEntity -> {

            // A sparkonto don't hold instruments
            if (!"sparkonto".equalsIgnoreCase(accountEntity.getAccountCode())) {
                return;
            }
            // accounts can be blocked, will have no further data
            if (accountEntity.isBlocked()) {
                log.debug(String.format("Account: %s blocked, reason: %s ",
                        accountEntity.getAccountNumber(), accountEntity.getBlockedReason()));
                return;
            }

            financialDataCacher.cacheAccount(accountEntity.toAccount(AccountTypes.SAVINGS));
        });
    }

    private void refreshInvestmentAccounts(AccountResponse accountEntities) {
        accountEntities.forEach(accountEntity -> {

            // A sparkonto don't hold instruments
            if (Objects.equals("sparkonto", Strings.nullToEmpty(accountEntity.getAccountCode()).toLowerCase())) {
                return;
            }
            // accounts can be blocked, will have no further data
            if (accountEntity.isBlocked()) {
                log.debug(String.format("Account: %s blocked, reason: %s ",
                        accountEntity.getAccountNumber(), accountEntity.getBlockedReason()));
                return;
            }

            Account account = accountEntity.toAccount(AccountTypes.INVESTMENT);
            Portfolio portfolio = accountEntity.toPortfolio();

            // Contains all positions, regardless of account/portfolio
            Optional<PositionsResponse> positions = apiClient.getPositions();

            if (!positions.isPresent()) {
                financialDataCacher.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
                return;
            }

            List<Instrument> instruments = Lists.newArrayList();
            for (PositionEntity positionEntity : positions.get()) {
                if (positionEntity.getAccountId().equalsIgnoreCase(accountEntity.getAccountId())) {
                    // If, this position actually belongs to this account/portfolio
                    positionEntity.toInstrument().ifPresent(instruments::add);
                }
            }
            portfolio.setInstruments(instruments);

            financialDataCacher.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private AccountResponse getAccounts() {

        if (accounts != null) {
            return accounts;
        }

        accounts = apiClient.fetchAccounts();
        return accounts;
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case SAVING_ACCOUNTS:
            refreshSavingsAccounts(getAccounts());
            break;

        case SAVING_TRANSACTIONS:
            // NOTE:
            // Last commit SHA related to this agent with transaction aggregation: feb1e91f9028a0a76e997b12146e2e4f1867e6d2
            // Though, since there are some transactions that are sent in other currencies we cannot get a complete view of
            // the account transactions and we thus removed the aggregation of them since they turned up in search and ID-koll.
            // If we want to reenable it we should investigate if amount is returned in SEK for all transactions in their
            // newer API that their app is using.
            break;

        case INVESTMENT_ACCOUNTS:
            refreshInvestmentAccounts(getAccounts());
            break;
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        resetToMobileBankIdIfNeeded();

        switch (credentials.getType()) {
        case PASSWORD:
            return loginWithPassword();
        case MOBILE_BANKID:
            return loginWithBankID();
        default:
            throw new IllegalStateException(String.format(
                    "Unsupported credentials type(%s)", credentials.getType()));
        }
    }

    /**
     * We've previously for BANKID and PASSWORD stored an access token for persistent login. Though it doesn't work
     * anymore, and it also wasn't a long-term access token so gave not much value.
     * <p>
     * Probably a keepAlive would work if we persisted cookies, but at this point there is just a small value of it.
     * <p>
     * For legacy reason this method is needed. We used to change a credential from BANKID to PASSWORD if access token
     * was present (as a kind of hack of not needing two factor on next login). Thus we need to check if we're password
     * and double check that the credentials has no password --> originally BANKID credential that should be reset so
     * that we can log it in again.
     */
    private void resetToMobileBankIdIfNeeded() {
        switch (credentials.getType()) {
        case PASSWORD:
            if (Strings.isNullOrEmpty(credentials.getField(Field.Key.PASSWORD))) {
                credentials.setType(CredentialsTypes.MOBILE_BANKID);
                systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
            }
            break;
        default:
            // Nothing
        }
    }

    private boolean loginWithPassword() throws LoginException {
        Optional<String> accessToken = apiClient.loginWithPassword(
                credentials.getField(Field.Key.USERNAME),
                credentials.getField(Field.Key.PASSWORD));

        Preconditions.checkState(accessToken.isPresent());

        return true;
    }

    private boolean loginWithBankID() throws BankIdException, LoginException {
        String orderRef = apiClient.initBankID(credentials.getField(Field.Key.USERNAME));
        openBankID();

        Optional<String> accessToken = collectBankID(orderRef);

        if (!accessToken.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        return true;
    }

    private Optional<String> collectBankID(String orderRef) throws BankIdException, LoginException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            BankIdStatus status = apiClient.collectBankId(orderRef);

            log.info(String.format("Collecting BankID, status: %s", status));

            switch (status) {
            case WAITING:
                break;
            case DONE:
                return apiClient.completeBankId(orderRef);
            case NO_CLIENT:
                throw BankIdError.NO_CLIENT.exception();
            default:
                throw new IllegalStateException(
                        "#login-refactoring - Unknown error detected while collecting BankID status: " + status);
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }
}
