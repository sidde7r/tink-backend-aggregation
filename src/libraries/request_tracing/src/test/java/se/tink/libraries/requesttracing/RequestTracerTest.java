package se.tink.libraries.requesttracing;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static se.tink.libraries.requesttracing.RequestTracer.REQUEST_ID_LENGTH;
import static se.tink.libraries.requesttracing.RequestTracer.REQUEST_ID_MDC_KEY;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;
import org.slf4j.MDC;

public class RequestTracerTest {
    @Test
    public void generateRequestIdIfEmptyRequestIdHeaders() {
        RequestTracer.startTracing(Optional.empty());
        assertEquals(REQUEST_ID_LENGTH, MDC.get(REQUEST_ID_MDC_KEY).length());
    }

    @Test
    public void useRequestIdFromHeader() {
        RequestTracer.startTracing(Optional.of("requestId"));
        assertEquals("requestId", MDC.get(REQUEST_ID_MDC_KEY));
    }

    @Test
    public void generateDifferentRequestIdEveryTime() {
        RequestTracer.startTracing(Optional.empty());
        String request1Id = MDC.get(REQUEST_ID_MDC_KEY);

        RequestTracer.startTracing(Optional.empty());
        String request2Id = MDC.get(REQUEST_ID_MDC_KEY);

        assertNotEquals(request1Id, request2Id);
    }

    @Test
    public void removeRequestIdFromMdcOnResponse() {
        RequestTracer.startTracing(Optional.empty());
        RequestTracer.stopTracing();
        assertNull(MDC.get(REQUEST_ID_MDC_KEY));
    }

    @Test
    public void mdcNotPropagatedToDifferentThread()
            throws ExecutionException, InterruptedException {
        RequestTracer.startTracing(Optional.empty());
        Future<Void> result =
                newSingleThreadExecutor()
                        .submit(
                                () -> {
                                    assertNull(MDC.get(REQUEST_ID_MDC_KEY));
                                    return null;
                                });
        result.get();
    }
}
