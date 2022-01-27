package se.tink.backend.aggregation.agents.authentication.options;

import org.junit.Ignore;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;

@Ignore
@AuthenticationOption(
        definition = AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE,
        overallDefault = true)
@AuthenticationOption(
        definition = AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE,
        overallDefault = true)
public class TestAgentWithMultipleOverallDefaultAuthenticationOptions extends BaseTestAgent {}
