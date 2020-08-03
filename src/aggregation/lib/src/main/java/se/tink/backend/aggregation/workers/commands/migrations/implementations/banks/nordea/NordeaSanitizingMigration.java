package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordea;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaSanitizingMigration extends ClusterSafeAgentVersionMigration {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String OLD_AGENT = "banks.nordea.NordeaAgent";
    private static final String NEW_AGENT = "nxgen.se.banks.nordea.v30.NordeaSEAgent";
    private static final String PASSWORD_KEY = "password";
    private static final String BANKID_PROVIDER = "nordea-bankid";
    private static final String PASSWORD_PROVIDER = "nordea-password";
    public static final String OXFORD_PRODUCTION = "oxford-production";
    public static final String OXFORD_STAGING = "oxford-staging";

    @Override
    public boolean isOldAgent(Provider provider) {
        return provider.getClassName().equals(OLD_AGENT);
    }

    @Override
    public boolean isNewAgent(Provider provider) {
        return provider.getClassName().equals(NEW_AGENT);
    }

    @Override
    public String getNewAgentClassName(Provider oldProvider) {
        return NEW_AGENT;
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        CredentialsTypes type = request.getCredentials().getType();
        if (type == CredentialsTypes.MOBILE_BANKID && hasPin(request)) {
            logger.info(
                    "encountered credentials with PIN to 'nordea-password' in cluster: "
                            + getClientIfo().getClusterId()
                            + " credentials: "
                            + getLogSafeString(request.getCredentials()));
        }
        return (type == CredentialsTypes.MOBILE_BANKID && !hasPin(request)
                        || type == CredentialsTypes.MOBILE_BANKID && hasPin(request) && !isOxford()
                        || type == CredentialsTypes.PASSWORD && hasPin(request) && isOxford())
                && request.getAccounts().stream()
                        .noneMatch(
                                acc ->
                                        acc.getBankId().contains("*")
                                                || acc.getBankId().contains(":"));
    }

    private String getLogSafeString(Credentials credentials) {

        Set<String> sensitiveKeys = credentials.getSensitivePayloadAsMap().keySet();
        Set<String> fields = credentials.getFields().keySet();

        return String.format(
                "{id:%s, payload:%s, provider:%s, status:%s, type:%s, userid:%s, sensitivePayload:%s, fields:%s}",
                credentials.getId(),
                credentials.getPayload(),
                credentials.getProviderName(),
                credentials.getStatus(),
                credentials.getType(),
                credentials.getUserId(),
                sensitiveKeys,
                fields);
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        CredentialsTypes credentialType =
                hasPin(request) && isOxford()
                        ? CredentialsTypes.PASSWORD
                        : CredentialsTypes.MOBILE_BANKID;

        String providerName = hasPin(request) && isOxford() ? PASSWORD_PROVIDER : BANKID_PROVIDER;

        request.getCredentials().setProviderName(providerName);
        request.getCredentials().setType(credentialType);
        request.getAccounts().forEach(a -> a.setBankId(sanitize(a.getBankId())));
    }

    private String sanitize(String string) {
        return string.replaceAll("[^A-Za-z0-9]", "");
    }

    private static boolean hasPin(CredentialsRequest request) {
        return !Strings.isNullOrEmpty(request.getCredentials().getSensitivePayload(PASSWORD_KEY));
    }

    private boolean isOxford() {
        return OXFORD_PRODUCTION.equals(getClientIfo().getClusterId())
                || OXFORD_STAGING.equals(getClientIfo().getClusterId());
    }
}
