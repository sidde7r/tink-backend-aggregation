package se.tink.backend.aggregation.workers.encryption.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class VersionDeserializerTest {

    @Test
    public void handleIsCalledWithoutDefaultHandlerThrowsIllegalStateException() {
        Throwable thrown = catchThrowable(() -> new VersionDeserializer().handle("{}"));
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handleIsCalledWithNullInputThrowsNullPointerException() {
        Throwable thrown =
                catchThrowable(
                        () -> new VersionDeserializer().setDefaultHandler(head -> {}).handle(null));
        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void handleConsumesVersion1HandlerOnV1Input() {
        String input =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"timestamp\": 1593186921015,\n"
                        + "  \"keyId\": 3,\n"
                        + "  \"fields\": {\n"
                        + "    \"iv\": \"aXYxCg==\",\n"
                        + "    \"data\": \"ZGF0YTEK\"\n"
                        + "  },\n"
                        + "  \"payload\": {\n"
                        + "    \"iv\": \"aXYyCg==\",\n"
                        + "    \"data\": \"ZGF0YTIK\"\n"
                        + "  }\n"
                        + "}";

        new VersionDeserializer()
                .setDefaultHandler(head -> Assert.fail("defualt handler should not be called"))
                .setVersion1Handler(
                        v1 -> Assert.assertEquals(1593186921015L, v1.getTimestamp().getTime()))
                .handle(input);
    }

    @Test
    public void handleConsumesDefaultHandlerOnNonImplementedInput() {
        String input =
                "{\n"
                        + "  \"version\": 99999,\n" // a high version with non-existing handler
                        + "  \"timestamp\": 123456789,\n"
                        + "  \"keyId\": 3,\n"
                        + "  \"fields\": {\n"
                        + "    \"iv\": \"aXYxCg==\",\n"
                        + "    \"data\": \"ZGF0YTEK\"\n"
                        + "  },\n"
                        + "  \"payload\": {\n"
                        + "    \"iv\": \"aXYyCg==\",\n"
                        + "    \"data\": \"ZGF0YTIK\"\n"
                        + "  }\n"
                        + "}";

        new VersionDeserializer()
                .setDefaultHandler(head -> Assert.assertEquals(99999, head.getVersion()))
                .setVersion1Handler(v1 -> Assert.fail("version1 handler should not be called"))
                .handle(input);
    }

    @Test
    public void handleConsumesSynchronously() {
        String input =
                "{\n"
                        + "  \"version\": 99999,\n" // a high version with non-existing handler
                        + "  \"timestamp\": 987654321,\n"
                        + "  \"keyId\": 3\n"
                        + "}";

        AtomicInteger incrementor = new AtomicInteger(0);

        incrementor.set(1);

        new VersionDeserializer()
                .setDefaultHandler(
                        head -> {
                            try {
                                Thread.sleep(3000);
                                incrementor.set(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        })
                .handle(input);

        Assert.assertEquals(2, incrementor.get());
        incrementor.set(3);
        Assert.assertEquals(3, incrementor.get());
    }
}
