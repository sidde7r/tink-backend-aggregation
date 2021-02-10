package se.tink.backend.aggregation.agents.brokers.lysa;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.brokers.lysa.model.AccountEntity;
import se.tink.backend.aggregation.agents.brokers.lysa.model.DetailsEntity;
import se.tink.backend.aggregation.agents.brokers.lysa.model.TransactionEntity;
import se.tink.backend.aggregation.agents.brokers.lysa.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.brokers.lysa.rpc.StartBankIdResponse;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class LysaAgent extends AbstractAgent implements RefreshInvestmentAccountsExecutor {
    private static final int MAX_ATTEMPTS = 60;

    private final LysaClient client;
    private DetailsEntity details;

    public LysaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        // The Lysa API needs fully buffered message bodies with content length.

        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);

        client =
                new LysaClient(
                        clientFactory.createCustomClient(
                                context.getLogOutputStream(), clientConfig),
                        CommonHeaders.DEFAULT_USER_AGENT);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return new FetchInvestmentAccountsResponse(
                details.getAccounts().stream()
                        .collect(
                                Collectors.toMap(
                                        AccountEntity::toAccount,
                                        a -> AccountFeatures.createEmpty())));
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return refreshInvestmentTransactions();
    }

    private FetchTransactionsResponse refreshInvestmentTransactions() {
        ImmutableListMultimap<String, TransactionEntity> transactionsByExternalAccountId =
                Multimaps.index(client.getTransactions(), TransactionEntity::getAccountId);

        return new FetchTransactionsResponse(
                details.getAccounts().stream()
                        .collect(
                                Collectors.toMap(
                                        AccountEntity::toAccount,
                                        a ->
                                                getTransactionsForAccount(
                                                        transactionsByExternalAccountId, a))));
    }

    private List<Transaction> getTransactionsForAccount(
            ImmutableListMultimap<String, TransactionEntity> transactionsByExternalAccountId,
            AccountEntity accountEntity) {
        List<Transaction> transactionsForAccount = Lists.newArrayList();

        for (TransactionEntity transactionEntity :
                transactionsByExternalAccountId.get(accountEntity.getAccountId())) {
            if (!transactionEntity.isValidTransaction()) {
                continue;
            }
            transactionsForAccount.add(transactionEntity.toTransaction());
        }
        return transactionsForAccount;
    }

    @Override
    public boolean login() throws Exception {
        Credentials credentials = request.getCredentials();

        StartBankIdResponse startBankIdResponse =
                client.startBankId(credentials.getField(Field.Key.USERNAME));

        supplementalInformationController.openMobileBankIdAsync(
                startBankIdResponse.getAutostartToken());

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            PollBankIdResponse pollBankIdResponse =
                    client.pollBankId(startBankIdResponse.getTransactionId());

            switch (pollBankIdResponse.getStatus()) {
                case PollBankIdResponse.Status.STARTED:
                case PollBankIdResponse.Status.OUTSTANDING_TRANSACTION:
                case PollBankIdResponse.Status.USER_SIGN:
                    Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
                    continue;
                case PollBankIdResponse.Status.COMPLETE:
                    details = pollBankIdResponse.getPayload().getDetails();
                    return true;
                case PollBankIdResponse.Status.NO_CLIENT:
                    throw BankIdError.NO_CLIENT.exception();
                case PollBankIdResponse.Status.EXPIRED_TRANSACTION:
                    throw BankIdError.TIMEOUT.exception();
                case PollBankIdResponse.Status.ABORTED:
                case PollBankIdResponse.Status.ERROR:
                    throw BankIdError.CANCELLED.exception();
                default:
                    throw new IllegalStateException(
                            String.format(
                                    "Unknown BankID status: %s", pollBankIdResponse.getStatus()));
            }
        }

        throw BankIdError.TIMEOUT.exception();
    }

    @Override
    public void logout() throws Exception {
        client.logout();
    }
}
