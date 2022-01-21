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
        Set<AuthenticationOptionDto> AuthenticationOptionDto =
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
        Set<AuthenticationOptionDto> AuthenticationOptionDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        assertThat(AuthenticationOptionDtoSet.size()).isEqualTo(1);

        List<AuthenticationOptionDto> AuthenticationOptionDtoList =
                new ArrayList<>(AuthenticationOptionDtoSet);
        AuthenticationOptionDto AuthenticationOptionDto = AuthenticationOptionDtoList.get(0);
        assertThat(AuthenticationOptionDto.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE.name());
        assertThat(AuthenticationOptionDto.isOverallDefault()).isTrue();
        assertThat(AuthenticationOptionDto.getDefaultForChannel())
                .isEqualTo(SupportedChannel.MOBILE);
    }

    @Test
    public void shouldReturnAuthenticationOptionsWhenMultipleSpecifiedInAgent() {
        // given
        Class<TestAgentWithMultipleAuthenticationOptions> klass =
                TestAgentWithMultipleAuthenticationOptions.class;
        extractor.validateAuthenticationOptions(klass);

        // when
        Set<AuthenticationOptionDto> AuthenticationOptionDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        assertThat(AuthenticationOptionDtoSet.size()).isEqualTo(2);

        List<AuthenticationOptionDto> AuthenticationOptionDtoList =
                AuthenticationOptionDtoSet.stream()
                        .sorted(Comparator.comparing(AuthenticationOptionDto::getName))
                        .collect(Collectors.toList());

        AuthenticationOptionDto AuthenticationOptionDtoOther = AuthenticationOptionDtoList.get(0);
        assertThat(AuthenticationOptionDtoOther.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE.name());
        assertThat(AuthenticationOptionDtoOther.getDefaultForChannel())
                .isEqualTo(SupportedChannel.DESKTOP);

        AuthenticationOptionDto AuthenticationOptionDtoSame = AuthenticationOptionDtoList.get(1);
        assertThat(AuthenticationOptionDtoSame.getName())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE.name());
        assertThat(AuthenticationOptionDtoSame.isOverallDefault()).isTrue();
        assertThat(AuthenticationOptionDtoSame.getDefaultForChannel())
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
