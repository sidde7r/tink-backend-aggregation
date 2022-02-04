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
    @AuthenticationOption(definition = AuthenticationOptionDefinition.SE_MOBILE_BANKID_SAME_DEVICE)
})
public class TestAgentWithRepeatedAuthenticationOptions extends BaseTestAgent {}
