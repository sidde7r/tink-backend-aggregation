package se.tink.backend.aggregation.agents.authentication.options;

import org.junit.Ignore;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOption.AuthenticationOptions;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;
import se.tink.libraries.authentication_options.AuthenticationOptionField;
import se.tink.libraries.authentication_options.SupportedChannel;

@Ignore
@AuthenticationOptions({
    @AuthenticationOption(
            definition = AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE,
            defaultForChannel = SupportedChannel.MOBILE,
            overallDefault = true),
    @AuthenticationOption(
            definition = AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE,
            defaultForChannel = SupportedChannel.DESKTOP,
            fields = {AuthenticationOptionField.SE_SOCIAL_SECURITY_NUMBER})
})
public class TestAgentWithMultipleAuthenticationOptions extends BaseTestAgent {}
