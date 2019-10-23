package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMasker;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;

public class LogMasker {

    /**
     * This enumeration decides if logging should be done or not. NOTE: Only pass
     * LOGGING_MASKER_COVERS_SECRETS if you are 100% certain that the masker will handle your
     * secrets. If that is not the case, you pass the other one. Or use {@link #shouldLog(Provider)}
     * instead.
     */
    public enum LoggingMode {
        LOGGING_MASKER_COVERS_SECRETS,
        UNSURE_IF_MASKER_COVERS_SECRETS
    }

    private final Iterable<StringMasker> stringMaskers;

    public LogMasker(Credentials credentials, Collection<String> sensitiveValuesToMask) {
        stringMaskers = createLogMaskers(credentials, sensitiveValuesToMask);
    }

    public String mask(String dataToMask) {
        if (dataToMask == null) {
            return null;
        }

        String masked = dataToMask;

        for (StringMasker masker : stringMaskers) {
            masked = masker.getMasked(masked);
        }

        return masked;
    }

    private Iterable<StringMasker> createLogMaskers(
            Credentials credentials, Collection<String> sensitiveValuesToMask) {
        StringMasker credentialsStringMasker =
                new CredentialsStringMasker(
                        credentials,
                        ImmutableList.of(
                                CredentialsStringMasker.CredentialsProperty.PASSWORD,
                                CredentialsStringMasker.CredentialsProperty.SECRET_KEY,
                                CredentialsStringMasker.CredentialsProperty.SENSITIVE_PAYLOAD,
                                CredentialsStringMasker.CredentialsProperty.USERNAME));
        StringMasker clientConfigurationStringMasker =
                new ClientConfigurationStringMasker(sensitiveValuesToMask);

        return ImmutableList.of(credentialsStringMasker, clientConfigurationStringMasker);
    }

    public static LoggingMode shouldLog(Provider provider) {
        // Temporary disable of http traffic logging for RE agents.
        // Leave until all RE agents logging has been evaluted and secrets moved to appropriate
        // format to be handled by logging masker.
        if (!provider.isOpenBanking()) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }

        if (
        /*!"FI".equalsIgnoreCase(provider.getMarket())
        && !"NO".equalsIgnoreCase(provider.getMarket())
        && !"DE".equalsIgnoreCase(provider.getMarket())
        && !"FR".equalsIgnoreCase(provider.getMarket())
        && !"IT".equalsIgnoreCase(provider.getMarket())
        &&*/ !"SE".equalsIgnoreCase(provider.getMarket())
                && !"ES".equalsIgnoreCase(provider.getMarket())) {
            return LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS;
        }
        return LoggingMode.LOGGING_MASKER_COVERS_SECRETS;
    }
}
