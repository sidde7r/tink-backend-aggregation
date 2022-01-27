package se.tink.backend.aggregation.agents.authentication.options;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;
import se.tink.libraries.authentication_options.AuthenticationOptionDto;
import se.tink.libraries.authentication_options.AuthenticationOptionField;
import se.tink.libraries.authentication_options.AuthenticationOptionsGroupDto;
import se.tink.libraries.authentication_options.Field;
import se.tink.libraries.authentication_options.SupportedChannel;

public class AuthenticationOptionsExtractorTest {

    private AuthenticationOptionsExtractor extractor;

    @Before
    public void setUp() {
        extractor = new AuthenticationOptionsExtractor(true);
    }

    @Test
    public void shouldReturnEmptySetWhenAgentHasNoAuthenticationOptions() {
        // given
        Class<TestAgentWithoutAuthenticationOptions> klass =
                TestAgentWithoutAuthenticationOptions.class;

        // when
        extractor.validateAuthenticationOptions(klass);
        Set<AuthenticationOptionsGroupDto> AuthenticationOptionDto =
                extractor.readAuthenticationOptions(klass);

        // then
        assertThat(AuthenticationOptionDto).isEmpty();
    }

    @Test
    public void shouldReturnAuthenticationOptionWhenOneSpecifiedInAgent() {
        // given
        Class<TestAgentWithOneAuthenticationOption> klass =
                TestAgentWithOneAuthenticationOption.class;

        // when
        extractor.validateAuthenticationOptions(klass);
        Set<AuthenticationOptionsGroupDto> authenticationOptionDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        assertThat(authenticationOptionDtoSet.size()).isEqualTo(1);

        List<AuthenticationOptionsGroupDto> AuthenticationOptionsGroupDtoList =
                new ArrayList<>(authenticationOptionDtoSet);
        AuthenticationOptionsGroupDto authenticationOptionsGroupDto =
                AuthenticationOptionsGroupDtoList.get(0);
        assertThat(authenticationOptionsGroupDto.getAuthenticationOptions().size()).isEqualTo(1);

        ArrayList<AuthenticationOptionDto> authenticationOptionDtoList =
                new ArrayList<>(authenticationOptionsGroupDto.getAuthenticationOptions());
        AuthenticationOptionDto authenticationOptionDto = authenticationOptionDtoList.get(0);
        assertThat(authenticationOptionDto.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE.name());
        assertThat(authenticationOptionDto.isOverallDefault()).isTrue();
        assertThat(authenticationOptionDto.getDefaultForChannel())
                .isEqualTo(SupportedChannel.MOBILE);
    }

    @Test
    public void shouldReturnAuthenticationOptionsWhenMultipleSpecifiedInAgent() {
        // given
        Class<TestAgentWithMultipleAuthenticationOptions> klass =
                TestAgentWithMultipleAuthenticationOptions.class;
        extractor.validateAuthenticationOptions(klass);

        // when
        Set<AuthenticationOptionsGroupDto> authenticationOptionsGroupDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        assertThat(authenticationOptionsGroupDtoSet.size()).isEqualTo(1);

        ArrayList<AuthenticationOptionsGroupDto> authenticationOptionsGroupDtos =
                new ArrayList<>(authenticationOptionsGroupDtoSet);
        AuthenticationOptionsGroupDto authenticationOptionsGroupDtoList =
                authenticationOptionsGroupDtos.get(0);
        List<AuthenticationOptionDto> authenticationOptionDtoList =
                authenticationOptionsGroupDtoList.getAuthenticationOptions().stream()
                        .sorted(Comparator.comparing(AuthenticationOptionDto::getName))
                        .collect(Collectors.toList());
        assertThat(authenticationOptionDtoList.size()).isEqualTo(2);

        AuthenticationOptionDto authenticationOptionDtoOther = authenticationOptionDtoList.get(0);
        assertThat(authenticationOptionDtoOther.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE.name());
        assertThat(authenticationOptionDtoOther.getDefaultForChannel())
                .isEqualTo(SupportedChannel.DESKTOP);

        Set<String> fieldsNames =
                authenticationOptionDtoOther.getFields().stream()
                        .map(Field::getName)
                        .collect(Collectors.toSet());
        assertThat(fieldsNames)
                .contains(AuthenticationOptionField.SE_SOCIAL_SECURITY_NUMBER.getField().getName());

        AuthenticationOptionDto authenticationOptionDtoSame = authenticationOptionDtoList.get(1);
        assertThat(authenticationOptionDtoSame.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE.name());
        assertThat(authenticationOptionDtoSame.isOverallDefault()).isTrue();
        assertThat(authenticationOptionDtoSame.getDefaultForChannel())
                .isEqualTo(SupportedChannel.MOBILE);
    }

    @Test
    public void shouldThrowWhenAuthenticationOptionIsChannelDefaultForNonSupportedChannel() {
        // given
        Class<TestAgentWithAuthenticationOptionChannelDefaultForNonSupportedChannel> klass =
                TestAgentWithAuthenticationOptionChannelDefaultForNonSupportedChannel.class;

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> extractor.validateAuthenticationOptions(klass));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        assertThat(throwable.getMessage())
                .isEqualTo(
                        "Agent authentication.options.TestAgentWithAuthenticationOptionChannelDefaultForNonSupportedChannel has an authentication option set as default for channel DESKTOP but that is not among its supported channels [MOBILE]");
    }

    @Test
    public void shouldThrowWhenMultipleChannelDefaultAuthenticationOptions() {
        // given
        Class<TestAgentWithMultipleChannelDefaultAuthenticationOptions> klass =
                TestAgentWithMultipleChannelDefaultAuthenticationOptions.class;

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> extractor.validateAuthenticationOptions(klass));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        assertThat(throwable.getMessage())
                .isEqualTo(
                        "Agent authentication.options.TestAgentWithMultipleChannelDefaultAuthenticationOptions has more than one authentication option which is the default for channel MOBILE");
    }

    @Test
    public void shouldThrowWhenMultipleOverallDefaultAuthenticationOptions() {
        // given
        Class<TestAgentWithMultipleOverallDefaultAuthenticationOptions> klass =
                TestAgentWithMultipleOverallDefaultAuthenticationOptions.class;

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> extractor.validateAuthenticationOptions(klass));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        assertThat(throwable.getMessage())
                .isEqualTo(
                        "Agent authentication.options.TestAgentWithMultipleOverallDefaultAuthenticationOptions has more than one authentication option with the flag overallDefault set to true");
    }

    @Test
    public void shouldThrowWhenRepeatedAuthenticationOptions() {
        // given
        Class<TestAgentWithRepeatedAuthenticationOptions> klass =
                TestAgentWithRepeatedAuthenticationOptions.class;

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> extractor.validateAuthenticationOptions(klass));

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        assertThat(throwable.getMessage())
                .isEqualTo(
                        "Agent authentication.options.TestAgentWithRepeatedAuthenticationOptions has more than one authentication option of type SE_BANKID_SAME_DEVICE");
    }
}
