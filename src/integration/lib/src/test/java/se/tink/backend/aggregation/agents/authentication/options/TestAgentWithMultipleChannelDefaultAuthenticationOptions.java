package se.tink.backend.aggregation.agents.authentication.options;

import org.junit.Ignore;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;
import se.tink.libraries.authentication_options.SupportedChannel;

@Ignore
@AuthenticationOption(
        definition = AuthenticationOptionDefinition.SE_BANKID_SAME_DEVICE,
        defaultForChannel = SupportedChannel.MOBILE,
        overallDefault = true)
@AuthenticationOption(
        definition = AuthenticationOptionDefinition.SE_BANKID_OTHER_DEVICE,
        defaultForChannel = SupportedChannel.MOBILE)
public class TestAgentWithMultipleChannelDefaultAuthenticationOptions extends BaseTestAgent {}
