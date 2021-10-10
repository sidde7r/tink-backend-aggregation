package se.tink.backend.aggregation.nxgen.http.log.executor.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogExchangeEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogMetaEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogRequestEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogResponseEntity;

/**
 * This is a class responsible for building a JSON log file with the whole agent's HTTP traffic. The
 * log has a fixed structure, and it will be stored in a dedicated s3 bucket to allow us to use
 * Athena queries effectively.
 */
@Slf4j
public class HttpJsonLogger {

    private static final LogTag LOG_TAG = LogTag.from("[HttpJsonLogger]");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpJsonLogMetaEntity metaEntity;
    private final Map<String, HttpJsonLogRequestEntity> requests = new LinkedHashMap<>();
    private final Map<String, HttpJsonLogResponseEntity> responses = new LinkedHashMap<>();

    public HttpJsonLogger(HttpJsonLogMetaEntity metaEntity) {
        this.metaEntity = metaEntity;
    }

    /**
     * Always call this method before running a new http exchange.
     *
     * <p>In HTTP JSON logs we want to match requests with responses and put them together into JSON
     * objects - having fixed structure like that will simplify queries in Athena. Since some agents
     * use parallel transactions fetching, we have to keep track of the current HTTP exchange by
     * assigning it some id.
     */
    public static void beforeHttpExchange() {
        String httpExchangeId = UUID.randomUUID().toString();
        HttpExchangeIdForCurrentThread.setValue(httpExchangeId);
    }

    /** Always call this method after running http exchange. */
    public static void afterHttpExchange() {
        HttpExchangeIdForCurrentThread.removeValue();
    }

    public void addRequestLog(HttpJsonLogRequestEntity requestEntity) {
        Optional<String> currentExchangeId = HttpExchangeIdForCurrentThread.getValue();

        if (!currentExchangeId.isPresent()) {
            log.warn("{} Ignoring http request log - no exchange id", LOG_TAG);
            return;
        }
        if (requests.containsKey(currentExchangeId.get())) {
            log.warn("{} Ignoring duplicate request log for the same HTTP exchange", LOG_TAG);
            return;
        }
        requests.put(currentExchangeId.get(), requestEntity);
    }

    public void addResponseLog(HttpJsonLogResponseEntity responseEntity) {
        Optional<String> currentExchangeId = HttpExchangeIdForCurrentThread.getValue();

        if (!currentExchangeId.isPresent()) {
            log.warn("{} Ignoring http response log - no exchange id", LOG_TAG);
            return;
        }
        if (responses.containsKey(currentExchangeId.get())) {
            log.warn("{} Ignoring duplicate response log for the same HTTP exchange", LOG_TAG);
            return;
        }
        responses.put(currentExchangeId.get(), responseEntity);
    }

    public Optional<String> tryGetLogContent() {
        HttpJsonLog httpLog = buildLog();
        try {
            return Optional.of(OBJECT_MAPPER.writeValueAsString(httpLog));
        } catch (JsonProcessingException e) {
            log.error("{} Could not serialize http json log", LOG_TAG, e);
            return Optional.empty();
        }
    }

    protected HttpJsonLog buildLog() {
        return HttpJsonLog.builder().meta(metaEntity).http(buildExchangeEntities()).build();
    }

    /*
    Join requests and responses into exchanges using their exchange ids
     */
    private List<HttpJsonLogExchangeEntity> buildExchangeEntities() {
        List<String> exchangeIdsWithRequest = new ArrayList<>(requests.keySet());
        List<String> exchangeIdsWithResponse = new ArrayList<>(responses.keySet());

        List<String> exchangeIdsWithOnlyRequest =
                ListUtils.subtract(exchangeIdsWithRequest, exchangeIdsWithResponse);
        if (!exchangeIdsWithOnlyRequest.isEmpty()) {
            log.warn(
                    "{} Missing response logs for exchanges: {}",
                    LOG_TAG,
                    exchangeIdsWithOnlyRequest);
        }

        List<String> exchangeIdsWithOnlyResponse =
                ListUtils.subtract(exchangeIdsWithResponse, exchangeIdsWithRequest);
        if (!exchangeIdsWithResponse.isEmpty()) {
            log.warn(
                    "{} Missing request logs for exchanges: {}",
                    LOG_TAG,
                    exchangeIdsWithOnlyResponse);
        }

        List<String> exchangeIdsWithBoth =
                ListUtils.intersection(exchangeIdsWithRequest, exchangeIdsWithResponse);
        return exchangeIdsWithBoth.stream()
                .map(
                        exchangeId ->
                                HttpJsonLogExchangeEntity.builder()
                                        .request(requests.get(exchangeId))
                                        .response(responses.get(exchangeId))
                                        .build())
                .collect(Collectors.toList());
    }

    /**
     * This class keeps current exchange id for all threads. Within a single thread, Tink's HTTP
     * client always sends request and then receives response - there should never be 2 subsequent
     * requests without a response in-between.
     */
    @Slf4j
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpExchangeIdForCurrentThread {

        private static final ThreadLocal<String> CURRENT_EXCHANGE_ID = new ThreadLocal<>();

        public static Optional<String> getValue() {
            return Optional.ofNullable(CURRENT_EXCHANGE_ID.get());
        }

        private static void setValue(String exchangeId) {
            if (CURRENT_EXCHANGE_ID.get() != null) {
                IllegalStateException exception =
                        new IllegalStateException("Previous http exchange log was not ended");
                log.warn("{} Removing previous http exchange id", LOG_TAG, exception);
                CURRENT_EXCHANGE_ID.remove();
            }

            CURRENT_EXCHANGE_ID.set(exchangeId);
        }

        private static void removeValue() {
            if (CURRENT_EXCHANGE_ID.get() == null) {
                IllegalStateException exception =
                        new IllegalStateException("Http exchange not started");
                log.warn("{} Could not remove previous http exchange id", LOG_TAG, exception);
            }

            CURRENT_EXCHANGE_ID.remove();
        }
    }
}
