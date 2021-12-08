package src.agent_sdk.sdk.test.user_interaction;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserInteractionType;
import se.tink.agent.sdk.user_interaction.supplemental_information.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.swedish_mobile_bankid.SwedishMobileBankIdInfo;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UserInteractionTest {
    @Test
    public void testThirdPartyApp() {
        UserInteraction<ThirdPartyAppInfo> userInteraction =
                UserInteraction.thirdPartyApp(ThirdPartyAppInfo.of(URL.of("foo://bar"))).build();

        Assert.assertEquals(UserInteractionType.THIRD_PARTY_APP, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String expectedPayload =
                "{\"android\":{\"packageName\":null,\"requiredMinimumVersion\":0,\"intent\":\"foo\"},\"ios\":{\"appStoreUrl\":null,\"scheme\":\"foo\",\"deepLinkUrl\":\"foo://bar\"},\"desktop\":{\"url\":\"foo://bar\"},\"downloadTitle\":null,\"downloadMessage\":null,\"upgradeTitle\":null,\"upgradeMessage\":null,\"state\":null}";
        String payload = userInteraction.getPayload();
        Assert.assertEquals(expectedPayload, payload);
    }

    @Test
    public void testSupplementalInformation() {

        SupplementalInformation supplementalInformation =
                SupplementalInformation.from(
                        Field.builder().name("foo").description("something").build(),
                        Field.builder().name("bar").description("something else").build());

        UserInteraction<SupplementalInformation> userInteraction =
                UserInteraction.supplementalInformation(supplementalInformation).build();

        Assert.assertEquals(
                UserInteractionType.SUPPLEMENTAL_INFORMATION, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String expectedPayload =
                "[{\"description\":\"something\",\"group\":null,\"helpText\":null,\"hint\":null,\"immutable\":false,\"masked\":false,\"maxLength\":null,\"minLength\":null,\"name\":\"foo\",\"numeric\":false,\"oneOf\":false,\"optional\":false,\"pattern\":null,\"patternError\":null,\"value\":null,\"sensitive\":false,\"style\":null,\"type\":null,\"checkbox\":false,\"additionalInfo\":null,\"selectOptions\":null},{\"description\":\"something else\",\"group\":null,\"helpText\":null,\"hint\":null,\"immutable\":false,\"masked\":false,\"maxLength\":null,\"minLength\":null,\"name\":\"bar\",\"numeric\":false,\"oneOf\":false,\"optional\":false,\"pattern\":null,\"patternError\":null,\"value\":null,\"sensitive\":false,\"style\":null,\"type\":null,\"checkbox\":false,\"additionalInfo\":null,\"selectOptions\":null}]";
        String payload = userInteraction.getPayload();
        Assert.assertEquals(expectedPayload, payload);
    }

    @Test
    public void testSwedishMobileBankIdWithAutostartToken() {
        UserInteraction<SwedishMobileBankIdInfo> userInteraction =
                UserInteraction.swedishMobileBankId(
                        SwedishMobileBankIdInfo.withAutostartToken(
                                "54b3d400-9cac-486e-8372-7001bb0b7d2c"));

        Assert.assertEquals(UserInteractionType.SWEDISH_MOBILE_BANKID, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String expectedPayload = "54b3d400-9cac-486e-8372-7001bb0b7d2c";
        String payload = userInteraction.getPayload();
        Assert.assertEquals(expectedPayload, payload);
    }

    @Test
    public void testSwedishMobileBankIdWithoutAutostartToken() {
        UserInteraction<SwedishMobileBankIdInfo> userInteraction =
                UserInteraction.swedishMobileBankId(
                        SwedishMobileBankIdInfo.withoutAutostartToken());

        Assert.assertEquals(UserInteractionType.SWEDISH_MOBILE_BANKID, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String payload = userInteraction.getPayload();
        Assert.assertNull(payload);
    }
}
