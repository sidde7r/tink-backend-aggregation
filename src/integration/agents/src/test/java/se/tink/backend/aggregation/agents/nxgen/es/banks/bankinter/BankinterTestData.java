package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Ignore
public class BankinterTestData {
    public static final String TEST_DATA_IBAN = "ES2201281337857486299388";
    private static final String TEST_DATA_PATH = "data/test/agents/es/bankinter/";

    public static <C> C loadTestResponse(String path, Class<C> responseClass) {
        try {
            final byte[] bytes = Files.readAllBytes(Paths.get(TEST_DATA_PATH, path));
            final String bodyString = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            final InputStream bodyStream = new ByteArrayInputStream(bytes);
            HttpResponse mockedResponse = mock(HttpResponse.class);
            doReturn(bodyString).when(mockedResponse).getBody(String.class);
            doReturn(bodyStream).when(mockedResponse).getBodyInputStream();
            if (responseClass == HttpResponse.class) {
                return (C) mockedResponse;
            } else {
                Constructor constructor = responseClass.getConstructor(String.class);
                return (C) constructor.newInstance(bodyString);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not load test response: " + path, e);
        }
    }
}
