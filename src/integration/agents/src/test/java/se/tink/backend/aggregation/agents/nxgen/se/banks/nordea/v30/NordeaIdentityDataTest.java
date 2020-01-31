package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.identitydata.rpc.FetchIdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.NameElement;
import se.tink.libraries.identitydata.NameElement.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaIdentityDataTest {

    @Test
    public void testIdentityDataParsing() {
        final FetchIdentityDataResponse response =
                SerializationUtils.deserializeFromString(
                        responseWithValidIdentityData, FetchIdentityDataResponse.class);
        IdentityData id = response.toTinkIdentityData();
        Assert.assertEquals(id.getNameElements().stream().count(), 2);
        final String firstName =
                id.getNameElements().stream()
                        .filter(e -> e.getType() == Type.FIRST_NAME)
                        .map(NameElement::getValue)
                        .findFirst()
                        .orElse("");
        Assert.assertEquals(firstName, "FIRSTNAME");
        final String surname =
                id.getNameElements().stream()
                        .filter(e -> e.getType() == Type.SURNAME)
                        .map(NameElement::getValue)
                        .findFirst()
                        .orElse("");
        Assert.assertEquals(surname, "LASTNAME");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIdentityParsing() {
        final FetchIdentityDataResponse response =
                SerializationUtils.deserializeFromString(
                        responseWithInvalidIdentityData, FetchIdentityDataResponse.class);
        response.toTinkIdentityData();
    }

    // identity response object as it was seen 2020-01-29
    private final String responseWithValidIdentityData =
            "{\n"
                    + "\t\"customer_id\": \"199201234567\",\n"
                    + "\t\"segment\": \"household\",\n"
                    + "\t\"loyalty_group\": \"\",\n"
                    + "\t\"person_id\": \"199201234567\",\n"
                    + "\t\"birth_date\": \"1992-01-23\",\n"
                    + "\t\"first_name\": \"FIRSTNAME\",\n"
                    + "\t\"last_name\": \"LASTNAME\",\n"
                    + "\t\"phone_number\": \"46712345678\",\n"
                    + "\t\"employee\": false,\n"
                    + "\t\"us_resident\": false\n"
                    + "}";

    private final String responseWithInvalidIdentityData =
            "{\n"
                    + "\t\"customer_id\": \"\",\n"
                    + "\t\"segment\": \"household\",\n"
                    + "\t\"loyalty_group\": \"\",\n"
                    + "\t\"person_id\": \"\",\n"
                    + "\t\"birth_date\": \"\",\n"
                    + "\t\"first_name\": \"\",\n"
                    + "\t\"last_name\": \"\",\n"
                    + "\t\"phone_number\": \"46712345678\",\n"
                    + "\t\"employee\": false,\n"
                    + "\t\"us_resident\": false\n"
                    + "}";
}
