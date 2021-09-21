package se.tink.backend.aggregation.storage.debug.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class S3StoragePathsProvider {

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final Credentials credentials;
    private final Provider provider;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    @Named("operationName")
    private String operationName;

    @Inject
    @Named("appId")
    private String appId;

    private static final String JSON_LOGS_MAIN_DIR = "bank-http-logs";

    /** Prepare a default path for AAP logs */
    public String getAapLogDefaultPath(String cleanLogContent) {
        LocalDateTime now = localDateTimeSource.now();
        return getCommonLogFileName(cleanLogContent, now) + ".log";
    }

    /** Prepare an LTS Payments' path for AAP logs */
    public String getAapLogsPaymentsLtsPath(String cleanLogContent) {
        LocalDateTime now = localDateTimeSource.now();
        String catalog =
                String.format(
                        "%s/%s/%s/%s",
                        getLongTermStorageDisputeBasePrefixFromConfig(),
                        now.getYear(),
                        now.getMonthValue(),
                        now.getDayOfMonth());

        String fileName = getCommonLogFileName(cleanLogContent, now);

        return String.format("%s/%s.log", catalog, fileName);
    }

    private String getLongTermStorageDisputeBasePrefixFromConfig() {
        return Optional.ofNullable(agentsServiceConfiguration)
                .map(AgentsServiceConfiguration::getAggregationWorker)
                .map(AggregationWorkerConfiguration::getLongTermStorageDisputeBasePrefix)
                .orElse("");
    }

    /** Prepare a path for JSON log */
    public String getJsonLogPath(String cleanLogContent) {
        LocalDateTime now = localDateTimeSource.now();
        String catalog =
                String.format(
                        "%s/%s/%s/%s/%s/%s",
                        JSON_LOGS_MAIN_DIR,
                        provider.getClassName(),
                        now.toLocalDate().toString(),
                        operationName,
                        provider.getName(),
                        appId);
        String fileName = getCommonLogFileName(cleanLogContent, now);
        return String.format("%s/%s.json", catalog, fileName);
    }

    private String getCommonLogFileName(String cleanLogContent, LocalDateTime now) {
        String providerName = provider.getName();
        String creationDate =
                ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE
                        .format(now.atZone(ZoneId.systemDefault()).toInstant())
                        .replace(":", ".");
        String userId = credentials.getUserId();
        String credentialsId = credentials.getId();
        String size = getBytesAndNumberOfLines(cleanLogContent);

        return String.format(
                "%s_%s_u%s_c%s_%s", providerName, creationDate, userId, credentialsId, size);
    }

    private static String getBytesAndNumberOfLines(String str) {
        int lines = str.split("\n").length;
        int bytesUtf8 = str.getBytes(StandardCharsets.UTF_8).length;
        return String.format("%dB_%d", bytesUtf8, lines);
    }
}
