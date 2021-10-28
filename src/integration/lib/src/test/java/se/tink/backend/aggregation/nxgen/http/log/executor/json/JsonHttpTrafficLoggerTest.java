package se.tink.backend.aggregation.nxgen.http.log.executor.json;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogExchangeEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogMetaEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogRequestEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogResponseEntity;

public class JsonHttpTrafficLoggerTest {

    private JsonHttpTrafficLogger jsonLogger;

    private static final HttpJsonLogMetaEntity META_ENTITY =
            HttpJsonLogMetaEntity.builder()
                    .providerName("providerName")
                    .agentName("agentName")
                    .operation("operation")
                    .requestId("requestId")
                    .userId("userId")
                    .credentialsId("credentialsId")
                    .clusterId("clusterId")
                    .appId("appId")
                    .build();
    private static final HttpJsonLogRequestEntity THREAD_1_REQUEST_1 =
            HttpJsonLogRequestEntity.builder()
                    .url("https://google.com?test=value")
                    .method("GET")
                    .body("some body w/e")
                    .headers(
                            ImmutableMap.of(
                                    "requestHeader1", "requestHeaderValue1",
                                    "requestHeader2", "requestHeaderValue2"))
                    .build();
    private static final HttpJsonLogResponseEntity THREAD_1_RESPONSE_1 =
            HttpJsonLogResponseEntity.builder()
                    .status(200)
                    .timestamp(LocalDateTime.of(2021, 1, 1, 11, 30))
                    .body("some response body")
                    .headers(
                            ImmutableMap.of(
                                    "responseHeader1", "responseHeaderValue1",
                                    "responseHeader2", "responseHeaderValue2"))
                    .build();
    private static final HttpJsonLogRequestEntity THREAD_2_REQUEST_1 =
            requestEntity("thread2 request1");
    private static final HttpJsonLogResponseEntity THREAD_2_RESPONSE_1 =
            responseEntity("thread2 response1");
    private static final HttpJsonLogRequestEntity THREAD_2_REQUEST_2 =
            requestEntity("thread2 request2");
    private static final HttpJsonLogResponseEntity THREAD_2_RESPONSE_2 =
            responseEntity("thread2 response2");

    @Test
    public void should_be_disabled_by_default() {
        // given
        jsonLogger = new JsonHttpTrafficLogger(META_ENTITY);

        // when
        boolean enabled = jsonLogger.isEnabled();

        // then
        assertThat(enabled).isFalse();
    }

    @Test
    public void should_correctly_build_json_log() {
        // given
        jsonLogger = new JsonHttpTrafficLogger(META_ENTITY);

        // when
        JsonHttpTrafficLogger.beforeHttpExchange();
        jsonLogger.addRequestLog(THREAD_1_REQUEST_1);
        jsonLogger.addResponseLog(THREAD_1_RESPONSE_1);
        JsonHttpTrafficLogger.afterHttpExchange();

        HttpJsonLog jsonLog = jsonLogger.buildLog();

        // then
        assertThat(jsonLog)
                .usingRecursiveComparison()
                .isEqualTo(
                        HttpJsonLog.builder()
                                .meta(META_ENTITY)
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_1_REQUEST_1)
                                                .response(THREAD_1_RESPONSE_1)
                                                .build())
                                .build());
    }

    @Test
    public void should_skip_logs_if_exchange_was_not_initialized() {
        // given
        jsonLogger = new JsonHttpTrafficLogger(META_ENTITY);

        // when
        jsonLogger.addRequestLog(THREAD_1_REQUEST_1);
        jsonLogger.addResponseLog(THREAD_1_RESPONSE_1);

        HttpJsonLog jsonLog = jsonLogger.buildLog();

        // then
        assertThat(jsonLog)
                .usingRecursiveComparison()
                .isEqualTo(HttpJsonLog.builder().meta(META_ENTITY).build());
    }

    @Test
    public void should_allow_response_to_come_before_request() {
        // given
        jsonLogger = new JsonHttpTrafficLogger(META_ENTITY);

        // when
        JsonHttpTrafficLogger.beforeHttpExchange();
        jsonLogger.addResponseLog(THREAD_1_RESPONSE_1);
        jsonLogger.addRequestLog(THREAD_1_REQUEST_1);
        JsonHttpTrafficLogger.afterHttpExchange();

        HttpJsonLog jsonLog = jsonLogger.buildLog();

        // then
        assertThat(jsonLog)
                .usingRecursiveComparison()
                .isEqualTo(
                        HttpJsonLog.builder()
                                .meta(META_ENTITY)
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_1_REQUEST_1)
                                                .response(THREAD_1_RESPONSE_1)
                                                .build())
                                .build());
    }

    @Test
    public void should_save_only_first_request_and_response_for_current_http_exchange() {
        // given
        jsonLogger = new JsonHttpTrafficLogger(META_ENTITY);

        // when
        JsonHttpTrafficLogger.beforeHttpExchange();
        jsonLogger.addRequestLog(THREAD_2_REQUEST_1);
        jsonLogger.addRequestLog(THREAD_2_REQUEST_1);

        jsonLogger.addResponseLog(THREAD_2_RESPONSE_1);
        jsonLogger.addResponseLog(THREAD_2_RESPONSE_2);
        JsonHttpTrafficLogger.afterHttpExchange();

        HttpJsonLog jsonLog = jsonLogger.buildLog();

        // then
        assertThat(jsonLog)
                .usingRecursiveComparison()
                .isEqualTo(
                        HttpJsonLog.builder()
                                .meta(META_ENTITY)
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_2_REQUEST_1)
                                                .response(THREAD_2_RESPONSE_1)
                                                .build())
                                .build());
    }

    @Test
    @SneakyThrows
    public void should_match_request_response_pairs_coming_from_multiple_threads() {
        // given
        jsonLogger = spy(new JsonHttpTrafficLogger(null));

        RequestsSynchronizer synchronizer = new RequestsSynchronizer();

        RequestExecutor thread1Executor =
                RequestExecutor.builder()
                        .name("thread1")
                        .synchronizer(synchronizer)
                        .addJob(
                                () -> {
                                    JsonHttpTrafficLogger.beforeHttpExchange();
                                    jsonLogger.addRequestLog(THREAD_1_REQUEST_1);
                                })
                        .addJob(
                                () -> {
                                    jsonLogger.addResponseLog(THREAD_1_RESPONSE_1);
                                    JsonHttpTrafficLogger.afterHttpExchange();
                                })
                        .build();

        RequestExecutor thread2Executor =
                RequestExecutor.builder()
                        .name("thread2")
                        .synchronizer(synchronizer)
                        .addJob(
                                () -> {
                                    JsonHttpTrafficLogger.beforeHttpExchange();
                                    jsonLogger.addRequestLog(THREAD_2_REQUEST_1);
                                })
                        .addJob(
                                () -> {
                                    jsonLogger.addResponseLog(THREAD_2_RESPONSE_1);
                                    JsonHttpTrafficLogger.afterHttpExchange();
                                })
                        .addJob(
                                () -> {
                                    JsonHttpTrafficLogger.beforeHttpExchange();
                                    jsonLogger.addRequestLog(THREAD_2_REQUEST_2);
                                })
                        .addJob(
                                () -> {
                                    jsonLogger.addResponseLog(THREAD_2_RESPONSE_2);
                                    JsonHttpTrafficLogger.afterHttpExchange();
                                })
                        .build();

        List<RequestExecutor> requestExecutors = asList(thread1Executor, thread2Executor);
        synchronizer.setExecutionOrder(
                "thread1", "thread2", "thread1", "thread2", "thread2", "thread2");

        // when
        List<Future<Void>> futures = submitTasks(Executors.newFixedThreadPool(2), requestExecutors);
        boolean finished = waitForFutures(futures);

        // then
        verify(jsonLogger).addRequestLog(THREAD_1_REQUEST_1);
        verify(jsonLogger).addRequestLog(THREAD_2_REQUEST_1);
        verify(jsonLogger).addResponseLog(THREAD_1_RESPONSE_1);
        verify(jsonLogger).addResponseLog(THREAD_2_RESPONSE_1);
        verify(jsonLogger).addRequestLog(THREAD_2_REQUEST_2);
        verify(jsonLogger).addResponseLog(THREAD_2_RESPONSE_2);

        assertThat(finished).isTrue();

        // when
        HttpJsonLog jsonLog = jsonLogger.buildLog();

        assertThat(jsonLog)
                .usingRecursiveComparison()
                .isEqualTo(
                        HttpJsonLog.builder()
                                .meta(null)
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_1_REQUEST_1)
                                                .response(THREAD_1_RESPONSE_1)
                                                .build())
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_2_REQUEST_1)
                                                .response(THREAD_2_RESPONSE_1)
                                                .build())
                                .addHttp(
                                        HttpJsonLogExchangeEntity.builder()
                                                .request(THREAD_2_REQUEST_2)
                                                .response(THREAD_2_RESPONSE_2)
                                                .build())
                                .build());
    }

    private static HttpJsonLogRequestEntity requestEntity(String body) {
        return HttpJsonLogRequestEntity.builder().body(body).build();
    }

    private static HttpJsonLogResponseEntity responseEntity(String body) {
        return HttpJsonLogResponseEntity.builder().body(body).build();
    }

    private static List<Future<Void>> submitTasks(
            ExecutorService executorService, List<RequestExecutor> requestExecutors) {
        List<Future<Void>> futures = new ArrayList<>();
        for (RequestExecutor requestExecutor : requestExecutors) {
            futures.add(executorService.submit(requestExecutor));
        }
        return futures;
    }

    private static boolean waitForFutures(List<Future<Void>> futures) {
        return futures.stream()
                .map(
                        future -> {
                            try {
                                future.get(5, TimeUnit.SECONDS);
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        })
                .reduce((b1, b2) -> b1 & b2)
                .orElse(false);
    }

    @Getter
    private static class RequestsSynchronizer {

        private final Semaphore semaphore;

        private Iterator<String> executorsInOrder;
        private String executorToRun;

        public RequestsSynchronizer() {
            semaphore = new Semaphore(1);
        }

        private void setExecutionOrder(String... executorNames) {
            this.executorsInOrder = asList(executorNames).iterator();
            this.executorToRun = this.executorsInOrder.next();
        }

        @SneakyThrows
        private void acquire() {
            semaphore.acquire();
        }

        private void release() {
            semaphore.release();
        }

        private void nextStep() {
            if (executorsInOrder.hasNext()) {
                executorToRun = executorsInOrder.next();
            }
        }
    }

    @Builder
    private static class RequestExecutor implements Callable<Void> {

        private final String name;
        private final RequestsSynchronizer synchronizer;

        @Singular("addJob")
        private final List<Runnable> jobs;

        @Override
        @SuppressWarnings("all")
        public Void call() {
            Iterator<Runnable> jobsToDo = jobs.iterator();

            while (jobsToDo.hasNext()) {
                synchronizer.acquire();
                if (synchronizer.getExecutorToRun().equals(name)) {
                    jobsToDo.next().run();
                    synchronizer.nextStep();
                }
                synchronizer.release();
            }

            return null;
        }
    }
}
