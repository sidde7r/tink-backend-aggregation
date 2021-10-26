package se.tink.backend.aggregation.storage.logs;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.logmasker.LogMasker;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentHttpLogsMasker {

    private final Credentials credentials;
    private final Provider provider;
    private final LogMasker logMasker;

    public String maskSensitiveOutputLog(String logContent) {
        for (Field providerField : provider.getFields()) {
            String credentialFieldValue = credentials.getField(providerField.getName());

            if (!Strings.isNullOrEmpty(credentialFieldValue)) {
                logContent =
                        logContent.replace(
                                credentialFieldValue, "***" + providerField.getName() + "***");
            }
        }

        if (logMasker == null) {
            log.warn("Log masker missing - returning empty logs");
            return "";
        }
        return logMasker.mask(logContent);
    }
}
