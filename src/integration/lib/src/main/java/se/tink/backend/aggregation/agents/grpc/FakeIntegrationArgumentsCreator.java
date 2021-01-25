package se.tink.backend.aggregation.agents.grpc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.user.rpc.User;

public class FakeIntegrationArgumentsCreator {

    public static CredentialsRequest getCredReq() {

        Provider provider = new Provider();
        provider.setClassName("nxgen.demo.banks.password.PasswordDemoAgent");
        provider.setName("se-test-password");
        provider.setMarket("SE");
        provider.setType(ProviderTypes.BANK);
        provider.setCurrency("SEK");

        se.tink.backend.agents.rpc.Credentials credentials =
                new se.tink.backend.agents.rpc.Credentials();
        credentials.setProviderName(provider.getName());
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUsername("tink");
        credentials.setFieldsSerialized("");
        credentials.setField("username", "tink");
        credentials.setStatus(CredentialsStatus.CREATED);

        User user = new User();

        CredentialsRequest credentialsRequest =
                new CredentialsRequest(user, provider, credentials) {
                    @Override
                    public boolean isManual() {
                        return false;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return CredentialsRequestType.CREATE;
                    }
                };
        return credentialsRequest;
    }

    public static AgentContext getAgentContext(MetricRegistry metricRegistry) {

        AgentContext context =
                new AgentContext() {
                    @Override
                    public String getProviderSessionCache() {
                        return null;
                    }

                    @Override
                    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {}

                    @Override
                    public Account updateTransactions(
                            Account account, List<Transaction> transactions) {
                        return null;
                    }

                    @Override
                    public void cacheIdentityData(IdentityData identityData) {}

                    @Override
                    public void cacheTransactions(
                            @Nonnull String accountUniqueId, List<Transaction> transactions) {}

                    @Override
                    public void cacheAccount(Account account, AccountFeatures accountFeatures) {}

                    @Override
                    public void updateStatus(CredentialsStatus status) {}

                    @Override
                    public void updateStatus(
                            CredentialsStatus status,
                            String statusPayload,
                            boolean statusFromProvider) {}

                    @Override
                    public Catalog getCatalog() {
                        return Catalog.getCatalog("sv_SE");
                    }

                    @Override
                    public void openBankId(String autoStartToken, boolean wait) {}

                    @Override
                    public String requestSupplementalInformation(
                            se.tink.backend.agents.rpc.Credentials credentials,
                            long waitFor,
                            TimeUnit timeUnit,
                            boolean wait) {
                        return null;
                    }

                    @Override
                    public Optional<String> waitForSupplementalInformation(
                            String key, long waitFor, TimeUnit unit) {
                        return Optional.empty();
                    }

                    @Override
                    public void processTransactions() {}

                    @Override
                    public Account sendAccountToUpdateService(String bankAccountId) {
                        return null;
                    }

                    @Override
                    public AccountHolder sendAccountHolderToUpdateService(
                            Account processedAccount) {
                        return null;
                    }

                    @Override
                    public void updateTransferDestinationPatterns(
                            Map<Account, List<TransferDestinationPattern>> map) {}

                    @Override
                    public void updateCredentialsExcludingSensitiveInformation(
                            se.tink.backend.agents.rpc.Credentials credentials,
                            boolean doStatusUpdate) {}

                    @Override
                    public List<Account> getUpdatedAccounts() {
                        return null;
                    }

                    @Override
                    public void updateEinvoices(List<Transfer> transfers) {}

                    @Override
                    public MetricRegistry getMetricRegistry() {
                        return metricRegistry;
                    }

                    @Override
                    public void sendIdentityToIdentityAggregatorService() {}
                };
        return context;
    }
}
