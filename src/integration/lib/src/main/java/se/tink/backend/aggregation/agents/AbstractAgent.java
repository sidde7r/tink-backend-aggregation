package se.tink.backend.aggregation.agents;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.FinancialDataCacher;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.utils.CookieContainer;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.net.client.TinkApacheHttpClient4;

/**
 * @deprecated Do not use the AbstractAgent. All existing agents extending AbstractAgent should
 *     migrate to a newer variant (e.g. AgentPlatformAgent).
 */
@Deprecated
public abstract class AbstractAgent extends SuperAbstractAgent {

    protected final JerseyClientFactory clientFactory;
    protected final StatusUpdater statusUpdater;
    protected final FinancialDataCacher financialDataCacher;
    protected final Logger log;
    protected final SupplementalInformationController supplementalInformationController;

    protected AbstractAgent(CredentialsRequest request, CompositeAgentContext context) {
        super(new AgentContextProviderImpl(request, context));
        this.statusUpdater = context;
        this.financialDataCacher = context;
        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(
                        context, request.getCredentials(), request.getState());
        this.clientFactory =
                new JerseyClientFactory(
                        context.getLogMasker(), LogMaskerImpl.shouldLog(request.getProvider()));
        // This is not a good practice, evaluate having loggers for each class as psf field
        this.log = LoggerFactory.getLogger(getAgentClass());
    }

    /** Returns the certain date for this account (that is from when we know we have all data) */
    protected Date getContentWithRefreshDate(Account account) {
        if (this.request.getAccounts() == null
                || this.request.getCredentials().getUpdated() == null) {
            return null;
        }

        Optional<Account> existingAccount =
                this.request.getAccounts().stream()
                        .filter(a -> (a.getBankId().equals(account.getBankId())))
                        .findFirst();

        if (!existingAccount.isPresent()) {
            return null;
        }

        return existingAccount.get().getCertainDate();
    }

    /**
     * Determine if we're content with the data that we've got from this run based on what we
     * already have in storage. Assumes the transaction list is in order as it comes from provider.
     */
    protected boolean isContentWithRefresh(Account account, List<Transaction> transactions) {

        if (transactions.size() == 0) {
            return false;
        }

        if (this.request.getAccounts() == null
                || this.request.getCredentials().getUpdated() == null) {
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

            int overlappingTransactionDays =
                    Math.abs(DateUtils.getNumberOfDaysBetween(t.getDate(), certainDate));

            if (transactionsBeforeCertainDate
                            >= AgentParsingUtils.SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS
                    && overlappingTransactionDays
                            >= AgentParsingUtils.SAFETY_THRESHOLD_NUMBER_OF_DAYS) {
                return true;
            }
        }
        return false;
    }

    /** Takes the cookies from the provided cookie container and add them to the client */
    protected void addSessionCookiesToClient(
            TinkApacheHttpClient4 client, CookieContainer cookieContainer) {
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
}
