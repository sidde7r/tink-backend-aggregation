package se.tink.backend.aggregation.agents;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.FinancialDataCacher;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.utils.CookieContainer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class AbstractAgent extends AgentParsingUtils implements Agent, AgentEventListener {
    public static final String AGENT_LOCK_PATTERN = "/locks/refreshCredentials/credentials/%s/%s";
    public static final String DEFAULT_USER_AGENT = "Tink (+https://www.tink.se/; noc@tink.se)";
    
    protected AgentsServiceConfiguration configuration;
    protected final JerseyClientFactory clientFactory;
    protected final AgentContext context;
    protected final AgentAggregatorIdentifier agentAggregatorIdentifier;
    protected final StatusUpdater statusUpdater;
    protected final SupplementalRequester supplementalRequester;
    protected final SystemUpdater systemUpdater;
    protected final CredentialsRequest request;
    protected final AggregationLogger log;
    protected final FinancialDataCacher financialDataCacher;

    protected AbstractAgent(CredentialsRequest request, AgentContext context) {
        this.request = request;
        this.context = context;
        this.agentAggregatorIdentifier = context;
        this.statusUpdater = context;
        this.financialDataCacher = context;
        this.supplementalRequester = context;
        this.systemUpdater = context;
        this.clientFactory = new JerseyClientFactory();

        this.log = new AggregationLogger(getAgentClass());
    }

    public AggregatorInfo getAggregatorInfo() {
        return agentAggregatorIdentifier.getAggregatorInfo();
    }

    @Override
    public Class<? extends Agent> getAgentClass() {
        return getClass();
    }

    /**
     * Returns the certain date for this account (that is from when we know we have all data)
     */
    protected Date getContentWithRefreshDate(Account account) {
        if (this.request.getAccounts() == null || this.request.getCredentials().getUpdated() == null) {
            return null;
        }

        Optional<Account> existingAccount = this.request.getAccounts().stream().filter(
                a -> (a.getBankId().equals(account.getBankId()))).findFirst();

        if (!existingAccount.isPresent()) {
            return null;
        }

        return existingAccount.get().getCertainDate();
    }

    /**
     * Determine if we're content with the data that we've got from this run based on what we already have in storage.
     * Assumes the transaction list is in order as it comes from provider.
     */
    protected boolean isContentWithRefresh(Account account,
            List<Transaction> transactions) {

        if (transactions.size() == 0) {
            return false;
        }

        if (this.request.getAccounts() == null || this.request.getCredentials().getUpdated() == null) {
            return false;
        }

        Date certainDate = getContentWithRefreshDate(account);

        if (certainDate == null) {
            return false;
        }

        // Reached certain date and check next SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS transactions
        // to not be after the previous one.

        Transaction lastTransaction = null;
        int transactionsBeforeCertainDate = 0;

        for (Transaction t : transactions) {

            if (lastTransaction == null) {

                if (t.getDate().before(certainDate)) {
                    lastTransaction = t;
                }
                continue;

            } else {

                // Certain date reached, check transaction is before last one.

                if (t.getDate().after(certainDate)) {

                    // If after, there is a gap in the paging. Start over again and
                    // find next transaction that is before certain date and do this again.

                    lastTransaction = null;
                    transactionsBeforeCertainDate = 0;

                } else {
                    transactionsBeforeCertainDate++;
                }
            }

            int overlappingTransactionDays = Math.abs(DateUtils.getNumberOfDaysBetween(t.getDate(), certainDate));

            if (transactionsBeforeCertainDate >= SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS &&
                    overlappingTransactionDays >= SAFETY_THRESHOLD_NUMBER_OF_DAYS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Default is to do nothing here, updated timestamp is set in updateService if status goes to UPDATED.
     */
    @Override
    public void onUpdateCredentialsStatus() {
        // Nothing.
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Takes the cookies from the provided cookie container and add them to the client
     */
    protected void addSessionCookiesToClient(TinkApacheHttpClient4 client, CookieContainer cookieContainer) {
        if (client == null) {
            this.log.error("Client is null");
            return;
        }

        addCookiesToContainer(client.getClientHandler().getCookieStore(), cookieContainer);
    }

    protected void addCookiesToContainer(CookieStore store, CookieContainer cookieContainer) {
        if (cookieContainer == null) {
            this.log.error("Cookie container is null");
            return;
        }

        if (store == null) {
            this.log.error("Cookie store is null");
            return;
        }

        for (Cookie cookie : cookieContainer.getCookies()) {
            store.addCookie(cookie);
        }
    }

    protected void clearSessionCookiesFromClient(CookieStore store) {
        store.clear();
    }

    @Override
    public void close() {
        // Deliberately left empty. Feel free to override for agents that need proper cleanup of resources.
    }

    protected void openBankID(String autostartToken) {
        Credentials credentials = this.request.getCredentials();
        credentials.setSupplementalInformation(autostartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        this.supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    protected void openBankID() {
        openBankID(null);
    }

    protected void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload) {
        Credentials credentials = this.request.getCredentials();
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(payload));
        credentials.setStatus(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);

        this.supplementalRequester.requestSupplementalInformation(credentials, false);
    }
}
