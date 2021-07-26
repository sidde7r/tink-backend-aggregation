package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageImpl;

@Ignore
public class DanskeBankExecutorHelperTest {
    String signText =
            "Ny Bankgirobetalning\n"
                    + "Från konto: Danske Konto 3300123456\n"
                    + "Text på kontoutdrag: 610550873500157\n"
                    + "Belopp: 7 138,00 SEK\n"
                    + "Datum: 2020-10-28\n"
                    + "Avgift: 0,00 SEK\n"
                    + "Betalningsmottagare: Bankgironummer 596-0158 KUNGSMYNTAN\n"
                    + "Referensnummer: 610550873500157\n";
    DanskeBankSEConfiguration configuration = new DanskeBankSEConfiguration();

    HttpResponse mockHttpResponse = mock(HttpResponse.class);

    @Test
    public void testSignaturePackageGenerating() throws IOException {
        String content =
                new String(
                        Files.readAllBytes(
                                Paths.get(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/js_body")));
        DanskeBankExecutorHelper helper =
                new DanskeBankExecutorHelper(
                        null,
                        "a872c1c2345b25c3dc2029fcee0e85a47abd144d",
                        configuration,
                        null,
                        new AgentTemporaryStorageImpl());
        when(mockHttpResponse.getBody(String.class)).thenReturn(content);
        String expect =
                "\"eyJIZWFkZXIiOnsiUHJvdG9jb2xWZXJzaW9uIjoiMy4wIiwiQ29tcG9uZW50VmVyc2lvbiI6IkVMNTI2MjAwIiwiU2VjdXJpdHlDb25jZXB0IjoiViIsIlNlY3VyaXR5QWN0aW9uIjoiMjEifSwiQWxnb3JpdGhtIjp7IkFsZ29yaXRobVR5cGUiOiI3In0sIk1lc3NhZ2UiOiJ0cnlRcnNMWjRTOCtZNlYzOW5uTjJlUXdpMWRkUXdObEVwalgzZ3lkd2FySUZVSVJYQ25ud0xRLzA3dmVaR21QcjI1NVNYejlrWDRialJMWklnQnp6Lyttb3Z1RFpvWWhUV1lieFNIdmorbHM3R2VuUzFSdDUxdW4vVnQ2RWVBK3ZBdm9kUWRLZzRzWUZpU1VOSmNWNEdWbVE1WVhLWktKUEZxWndQTndpcEFlZm9VYnp1dW5salMweDBpaUVLcHZtK0piOTlzVGxxdlI5Uit2QldWRHNsNzBkbGF4ODM3ZWc3eFNjMzFOWTZHYWFscWZkbE1lK080Vm54TFprM3kvYWZZcTZldkpUUU9aRlR6S05DVldHMExqcFU4OThsdmpXSWxWc2FHaE5vVmJPazR1RFNmR1YxYjA4RExBazQ0YzI4aGFEOGhCbWRRa2xCb1ZmV3ZHUWZZVUJtZjNyZ0xuTWMrNlJsamU0Mms2U2dWNVlKK1VWVUk5QnkwVjdZdW8rTks5YS9VN3NFUE1ndTd1YTNtYkxIemE1V0tLUlYvbGd0QTN3WUpSdEZ4RTFiTmRwZG9ja0VrOE14R0ZtQ2NsSFJKYlFWTnRkc3F5SVN2T29ITktEeUhuVkZMWWZLRlJ6T1N1elhDdTZIdzhYU0czOTY4c0lXM2pXcm9nMmtHTDBXUmtyOFI4YnJGVGZUQ0ZSNE9xTFdZNUJvNXVTZGlodW9ucnM5cTl3RTE4a0pYUUUzMENkNHhrbkRkQVhhR2JCVURaVjFIaDR2dzRMMWszZU4vZTlLQXJFWW1sODFZdE5haFg2cFM1T0N5YlVyYlBIOXNSWGp6YVdnUlFrakdHNlVuYnVjWXlpeHBabWcvSndsWGZpeGRnMGN5ODNaOWtrZ1VVQWYvZXVHY3pwaGljSkZsRVpPMDdkZTU2dnhHbStJNmMwZDh2WVRuVVd5ZUhCZXZMWEdYUWVCSkkyOXRLTGJhWS9Eak9FY2RnWWJxY0M4TzJRY0lNamVWeitWYXdWTXJyaW5YajBraUJSdHRnRVVwbFYwTkFDOGxPSzVENlcyTEE1SFJFbGNRN05XV3FWdkZSTlZoQ0lHV3RnRjEvenF1bHdkRXJ2c1ZJWXNuVjhZKzhWUT09In0=\"";
        Assert.assertEquals(
                expect, helper.getSignaturePackage(mockHttpResponse, "***MASKED***", signText));
    }
}
