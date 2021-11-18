package se.tink.backend.aggregation.storage.logs;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.libraries.har_logger.src.model.HarEntry;
import se.tink.libraries.har_logger.src.model.MaskedHarEntry;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarMasker;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarMaskerImpl;
import src.libraries.aggregation_http_loggable_headers.src.AggregationHttpLoggableHeaders;

@Slf4j
public class AgentHttpLogsMasker implements HarMasker {

    private final Credentials credentials;
    private final Provider provider;
    private final LogMasker logMasker;

    private final HarMasker harMasker =
            new HarMaskerImpl(
                    AggregationHttpLoggableHeaders::isHttpHeaderLoggable,
                    this::maskSensitiveOutputLog);

    @Inject
    public AgentHttpLogsMasker(Credentials credentials, Provider provider, LogMasker logMasker) {
        this.credentials = credentials;
        this.provider = provider;
        this.logMasker = logMasker;
    }

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

    @Override
    public MaskedHarEntry mask(HarEntry entry) {
        return harMasker.mask(entry);
    }
}
