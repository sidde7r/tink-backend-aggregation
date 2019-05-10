package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.fetcher.identitydata;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.AlandsBankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.IdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.NameElement;
import se.tink.libraries.identitydata.NameElement.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AlandsBankenSeIdentityDataFetcherTest {

    private final String identityDataResponse =
            "{\n"
                    + "  \"status\": {\n"
                    + "    \"success\": true,\n"
                    + "    \"errors\": [],\n"
                    + "    \"infos\": [],\n"
                    + "    \"jSessionId\": \"zl4uG4k-bbOjHNHe4NXva4yj\"\n"
                    + "  },\n"
                    + "  \"ssn\": \"121212-1212\",\n"
                    + "  \"customerType\": \"REGULAR\",\n"
                    + "  \"gender\": \"MALE\",\n"
                    + "  \"externalId\": null,\n"
                    + "  \"firstName\": \"FIRSTNAME\",\n"
                    + "  \"lastName\": \"LASTNAME\",\n"
                    + "  \"address\": \"SOME ADDRESS 4\",\n"
                    + "  \"postalCode\": \"12345\",\n"
                    + "  \"postOffice\": \"STOCKHOLM\",\n"
                    + "  \"locale\": \"sv_SE\",\n"
                    + "  \"countryCode\": \"SE\",\n"
                    + "  \"addressCountryCode\": \"SE\",\n"
                    + "  \"serviceCodes\": [],\n"
                    + "  \"lastLogin\": \"19.02.2019 20.18.36\",\n"
                    + "  \"emailAddressHome\": \"name@mail.com\",\n"
                    + "  \"emailAddressWork\": \"\",\n"
                    + "  \"faxNumber\": \"\",\n"
                    + "  \"phoneNumber\": \"\",\n"
                    + "  \"phoneNumberWork\": \"\",\n"
                    + "  \"mobileNumber\": \"461234567890\",\n"
                    + "  \"mobileNumberWork\": \"\",\n"
                    + "  \"advisorName\": null,\n"
                    + "  \"advisorPhoneNumber\": null,\n"
                    + "  \"advisorImageName\": null,\n"
                    + "  \"defaultAdvisorImageName\": null,\n"
                    + "  \"domesticPhoneNumber\": \"112345678901\",\n"
                    + "  \"domesticPhoneNumberWork\": \"0 \",\n"
                    + "  \"domesticMobileNumber\": \"112345678901\",\n"
                    + "  \"domesticMobileNumberWork\": \"0 \",\n"
                    + "  \"bankOffice\": 34\n"
                    + "}";

    @Test
    public void testIdentityDataParsing() {
        AlandsBankenSEConfiguration config = new AlandsBankenSEConfiguration();
        IdentityDataResponse resp =
                SerializationUtils.deserializeFromString(
                        identityDataResponse, IdentityDataResponse.class);
        IdentityData identityData = config.parseIdentityData(resp);
        Assert.assertEquals(identityData.getNameElements().size(), 2);
        NameElement firstName =
                identityData.getNameElements().stream()
                        .filter(name -> name.getType() == Type.FIRST_NAME)
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        Assert.assertTrue(firstName.getValue().equals("FIRSTNAME"));
        NameElement surname =
                identityData.getNameElements().stream()
                        .filter(name -> name.getType() == Type.SURNAME)
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        Assert.assertTrue(surname.getValue().equals("LASTNAME"));
        Assert.assertEquals(identityData.getSsn(), "191212121212");
    }
}
