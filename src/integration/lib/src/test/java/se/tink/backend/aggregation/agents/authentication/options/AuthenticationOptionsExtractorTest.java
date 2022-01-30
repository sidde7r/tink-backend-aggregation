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

public class AuthenticationOptionsExtractorTest {

    private AuthenticationOptionsExtractor extractor;

    @Before
    public void setUp() {
        extractor = new AuthenticationOptionsExtractor(false);
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
        Set<AuthenticationOptionsGroupDto> authenticationOptionsGroupsDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        // one authentication options group found
        assertThat(authenticationOptionsGroupsDtoSet.size()).isEqualTo(1);

        // one authentication option found in that group
        List<AuthenticationOptionsGroupDto> AuthenticationOptionsGroupDtoList =
                new ArrayList<>(authenticationOptionsGroupsDtoSet);
        AuthenticationOptionsGroupDto authenticationOptionsGroupDto =
                AuthenticationOptionsGroupDtoList.get(0);
        assertThat(authenticationOptionsGroupDto.getAuthenticationOptions().size()).isEqualTo(1);

        // assertions on that authentication option found, verifying it is the one we expect
        ArrayList<AuthenticationOptionDto> authenticationOptionDtoList =
                new ArrayList<>(authenticationOptionsGroupDto.getAuthenticationOptions());
        AuthenticationOptionDto authenticationOptionDto = authenticationOptionDtoList.get(0);
        assertThat(authenticationOptionDto.getDefinition())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE);
        assertThat(authenticationOptionDto.isOverallDefault()).isTrue();
    }

    @Test
    public void shouldReturnAuthenticationOptionsWhenMultipleSpecifiedInAgent() {
        // given
        Class<TestAgentWithMultipleAuthenticationOptions> klass =
                TestAgentWithMultipleAuthenticationOptions.class;
        extractor.validateAuthenticationOptions(klass);

        // when
        Set<AuthenticationOptionsGroupDto> authenticationOptionsGroupsDtoSet =
                extractor.readAuthenticationOptions(klass);

        // then
        // one authentication options group found
        assertThat(authenticationOptionsGroupsDtoSet.size()).isEqualTo(1);

        // two authentication options found in that group
        ArrayList<AuthenticationOptionsGroupDto> authenticationOptionsGroupDtos =
                new ArrayList<>(authenticationOptionsGroupsDtoSet);
        AuthenticationOptionsGroupDto authenticationOptionsGroupDtoList =
                authenticationOptionsGroupDtos.get(0);
        List<AuthenticationOptionDto> authenticationOptionDtoList =
                authenticationOptionsGroupDtoList.getAuthenticationOptions().stream()
                        .sorted(
                                Comparator.comparing(
                                        authenticationOptionDto ->
                                                authenticationOptionDto.getDefinition().name()))
                        .collect(Collectors.toList());
        assertThat(authenticationOptionDtoList.size()).isEqualTo(2);

        // assertions on the first of authentication options, including verification of fields
        AuthenticationOptionDto authenticationOptionDtoOther = authenticationOptionDtoList.get(0);
        assertThat(authenticationOptionDtoOther.getDefinition())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE);

        Set<String> fieldsNames =
                authenticationOptionDtoOther.getFields().stream()
                        .map(AuthenticationOptionField::getField)
                        .map(Field::getName)
                        .collect(Collectors.toSet());
        assertThat(fieldsNames)
                .contains(AuthenticationOptionField.SE_SOCIAL_SECURITY_NUMBER.getField().getName());

        // assertions on the second authentication option
        AuthenticationOptionDto authenticationOptionDtoSame = authenticationOptionDtoList.get(1);
        assertThat(authenticationOptionDtoSame.getDefinition())
                .isEqualTo(AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE);
        assertThat(authenticationOptionDtoSame.isOverallDefault()).isTrue();
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
