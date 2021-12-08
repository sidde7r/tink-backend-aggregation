package src.agent_sdk.sdk.test.user_interaction;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserInteractionType;
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
        ImmutableList<Field> fields =
                ImmutableList.<Field>builder()
                        .add(Field.builder().name("foo").description("something").build())
                        .add(Field.builder().name("bar").description("something else").build())
                        .build();
        UserInteraction<ImmutableList<Field>> userInteraction =
                UserInteraction.supplementalInformation(fields).build();

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
        UserInteraction<String> userInteraction =
                UserInteraction.swedishMobileBankId("54b3d400-9cac-486e-8372-7001bb0b7d2c");

        Assert.assertEquals(UserInteractionType.SWEDISH_MOBILE_BANKID, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String expectedPayload = "54b3d400-9cac-486e-8372-7001bb0b7d2c";
        String payload = userInteraction.getPayload();
        Assert.assertEquals(expectedPayload, payload);
    }

    @Test
    public void testSwedishMobileBankIdWithoutAutostartToken() {
        UserInteraction<String> userInteraction = UserInteraction.swedishMobileBankId(null);

        Assert.assertEquals(UserInteractionType.SWEDISH_MOBILE_BANKID, userInteraction.getType());
        Assert.assertEquals(Optional.empty(), userInteraction.getCustomResponseKey());
        Assert.assertFalse(userInteraction.isUserResponseRequired());

        String payload = userInteraction.getPayload();
        Assert.assertNull(payload);
    }
}
