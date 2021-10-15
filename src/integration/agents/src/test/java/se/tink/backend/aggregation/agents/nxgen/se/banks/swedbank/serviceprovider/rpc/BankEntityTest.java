package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankEntityTest {

    @Test
    public void shouldReturnPrivateProfileIfGivenProfileIdIsSameAsPrivateProfileId() {

        BankEntity bankEntity =
                SerializationUtils.deserializeFromString(getPrivateProfile(), BankEntity.class);

        ProfileEntity result = bankEntity.getProfile("270d2a38e0c854836df492dc0535e4e1071a4718");

        assertTrue(result instanceof PrivateProfileEntity);
    }

    @Test
    public void shouldReturnBusinessProfileIfGivenProfileIdIsSameAsBusinessProfile() {
        BankEntity bankEntity =
                SerializationUtils.deserializeFromString(getBusinessProfile(), BankEntity.class);

        ProfileEntity result = bankEntity.getProfile("9a8ce02e009a8ce02e009a8ce02e009a8ce02e00");

        assertTrue(result instanceof BusinessProfileEntity);
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfGivenIdDoesNotExistButThereAreProfiles() {
        BankEntity bankEntity =
                SerializationUtils.deserializeFromString(getBusinessProfile(), BankEntity.class);

        Throwable throwable = catchThrowable(() -> bankEntity.getProfile("fakeID"));

        assertThat(throwable).isExactlyInstanceOf(IllegalStateException.class);
        assertEquals("Could not find profile fakeID", throwable.getMessage());
    }

    @Test
    public void shouldThrowIllegalStateExceptionThereAreNoProfiles() {
        BankEntity bankEntity =
                SerializationUtils.deserializeFromString(getNoProfilesResponse(), BankEntity.class);

        Throwable throwable = catchThrowable(() -> bankEntity.getProfile("fakeID"));

        assertThat(throwable).isExactlyInstanceOf(IllegalStateException.class);
        assertEquals("Profile not found", throwable.getMessage());
    }

    private String getPrivateProfile() {
        return "{"
                + "\"name\":\"Swedbank AB (publ)\","
                + "\"bankId\":\"08999\","
                + "\"url\":\"https://www.swedbank.se\","
                + "\"privateProfile\":"
                + "{\"activeProfileLanguage\":\"sv\","
                + "\"targetType\":\"PRIVATE\","
                + "\"customerName\":\"Esbjorn Fakename\","
                + "\"customerNumber\":\"19681210-7768\","
                + "\"id\":\"270d2a38e0c854836df492dc0535e4e1071a4718\","
                + "\"bankName\":\"Swedbank AB (publ)\","
                + "\"customerInternational\":false,"
                + "\"youthProfile\":false,"
                + "\"links\":"
                + "{\"edit\":"
                + "{\"method\":\"PUT\","
                + "\"uri\":\"/v5/profile/subscription/270d2a38e0c854836df492dc0535e4e1071a4718\"},"
                + "\"next\":"
                + "{\"method\":\"POST\","
                + "\"uri\":\"/v5/profile/270d2a38e0c854836df492dc0535e4e1071a4718\"}}},"
                + "\"corporateProfiles\":[],"
                + "\"hasSwedbankProfile\":true,"
                + "\"hasSavingbankProfile\":false,"
                + "\"userId\":\"***MASKED***\""
                + "}";
    }

    private String getBusinessProfile() {
        return "{"
                + "\"name\":\"Sparbanken Göinge AB\","
                + "\"bankId\":\"08403\","
                + "\"url\":\"https://www.sparbankengoinge.se\","
                + "\"corporateProfiles\":"
                + "[{\"activeProfileName\":\"Other Company 2 AB\","
                + "\"activeProfileLanguage\":\"sv\","
                + "\"targetType\":\"CORPORATE\","
                + "\"customerName\":\"Esbjorn Testsson\","
                + "\"customerNumber\":\"555555-7777\","
                + "\"id\":\"9a8ce02e009a8ce02e009a8ce02e009a8ce02e00\","
                + "\"bankName\":\"Sparbanken Göinge AB\","
                + "\"customerInternational\":false,"
                + "\"youthProfile\":false,"
                + "\"links\":{"
                + "\"edit\":{"
                + "\"method\":\"PUT\","
                + "\"uri\":\"/v5/profile/subscription/9a8ce02e009a8ce02e009a8ce02e009a8ce02e00\"},"
                + "\"next\":{\"method\":\"POST\","
                + "\"uri\":\"/v5/profile/9a8ce02e009a8ce02e009a8ce02e009a8ce02e00\"}}}]},";
    }

    private String getNoProfilesResponse() {
        return "{"
                + "\"name\":\"Sparbanken Göinge AB\","
                + "\"bankId\":\"08403\","
                + "\"url\":\"https://www.sparbankengoinge.se\","
                + "\"bankName\":\"Sparbanken Göinge AB\","
                + "\"customerInternational\":false,"
                + "\"youthProfile\":false,"
                + "\"links\":{"
                + "\"edit\":{"
                + "\"method\":\"PUT\","
                + "\"uri\":\"/v5/profile/subscription/9a8ce02e009a8ce02e009a8ce02e009a8ce02e00\"},"
                + "\"next\":{\"method\":\"POST\","
                + "\"uri\":\"/v5/profile/9a8ce02e009a8ce02e009a8ce02e009a8ce02e00\"}}}]},";
    }
}
