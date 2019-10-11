package se.tink.backend.aggregation.log;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMasker;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;

public class LogMasker {
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
}
