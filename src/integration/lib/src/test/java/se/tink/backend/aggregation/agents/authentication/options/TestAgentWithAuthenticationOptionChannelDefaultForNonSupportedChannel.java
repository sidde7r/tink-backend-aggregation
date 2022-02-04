package se.tink.backend.aggregation.agents.authentication.options;

import org.junit.Ignore;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOption.AuthenticationOptions;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;

@Ignore
@AuthenticationOptions({
    @AuthenticationOption(
            definition = AuthenticationOptionDefinition.SE_MOBILE_BANKID_SAME_DEVICE,
            overallDefault = true),
    @AuthenticationOption(definition = AuthenticationOptionDefinition.SE_MOBILE_BANKID_OTHER_DEVICE)
})
public class TestAgentWithAuthenticationOptionChannelDefaultForNonSupportedChannel
        extends BaseTestAgent {}
