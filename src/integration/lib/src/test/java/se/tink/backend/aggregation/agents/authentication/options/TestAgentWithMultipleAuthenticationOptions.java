package se.tink.backend.aggregation.agents.authentication.options;

import org.junit.Ignore;
import se.tink.libraries.authentication_options.AuthenticationOption;
import se.tink.libraries.authentication_options.AuthenticationOption.AuthenticationOptions;
import se.tink.libraries.authentication_options.AuthenticationOptionDefinition;
import se.tink.libraries.authentication_options.AuthenticationOptionField;

@Ignore
@AuthenticationOptions({
    @AuthenticationOption(
            definition = AuthenticationOptionDefinition.SE_MOBILE_BANKID_SAME_DEVICE,
            overallDefault = true),
    @AuthenticationOption(
            definition = AuthenticationOptionDefinition.SE_MOBILE_BANKID_OTHER_DEVICE,
            fields = {AuthenticationOptionField.SE_SOCIAL_SECURITY_NUMBER})
})
public class TestAgentWithMultipleAuthenticationOptions extends BaseTestAgent {}
