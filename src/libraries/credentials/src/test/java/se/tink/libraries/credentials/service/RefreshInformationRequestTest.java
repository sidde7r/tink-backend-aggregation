package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public final class RefreshInformationRequestTest {

    @Test
    public void testDeserialization_whenLocaleIsSet_localeIsFound() throws IOException {

        final String serialized =
                "{\"credentials\": null, \"provider\": null, \"user\": {\"flags\": [], \"id\": \"77777777777777777777777777777777\", \"profile\": {\"locale\": \"sv_SE\"}, \"username\": null, \"debugUntil\": null}, \"userDeviceId\": null, \"accounts\": [], \"create\": false, \"update\": false, \"callbackRedirectUriId\": null, \"manual\": false, \"itemsToRefresh\": null}";

        final ObjectMapper stringMapper =
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final RefreshInformationRequest request =
                stringMapper.readValue(serialized, RefreshInformationRequest.class);

        Assert.assertEquals("sv_SE", request.getUser().getLocale());
    }
}
