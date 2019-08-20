package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

public final class ProgressiveAuthAgentTest {

    private abstract static class AgentStub implements ProgressiveAuthAgent {

        @Override
        public void setConfiguration(final AgentsServiceConfiguration configuration) {}

        @Override
        public Class<? extends Agent> getAgentClass() {
            return null;
        }

        @Override
        public boolean login() {
            return false;
        }

        @Override
        public void logout() {}

        @Override
        public void close() {}
    }

    @Test
    public void testSingleStep() throws Exception {

        final ProgressiveAuthAgent agent =
                new AgentStub() {
                    @Override
                    public SteppableAuthenticationResponse login(
                            final SteppableAuthenticationRequest request) {

                        final AuthenticationResponse payload =
                                new AuthenticationResponse(Collections.emptyList());

                        return SteppableAuthenticationResponse.finalResponse(payload);
                    }
                };

        final SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.initialRequest();

        final SteppableAuthenticationResponse response = agent.login(request);

        Assert.assertEquals(Optional.empty(), response.getStep());
        Assert.assertEquals(Collections.emptyList(), response.getPayload().getFields());
    }

    @Test
    public void testTwoSteps() throws Exception {

        class LoginStep implements AuthenticationStep {

            @Override
            public AuthenticationResponse respond(final AuthenticationRequest request) {

                // Unless the following is true, the authenticator would throw INCORRECT_CREDENTIALS
                Assert.assertEquals(1, request.getUserInputs().size());
                Assert.assertEquals("133700", request.getUserInputs().get(0));

                return new AuthenticationResponse(Collections.emptyList());
            }
        }

        final ProgressiveAuthAgent agent =
                new AgentStub() {
                    @Override
                    public SteppableAuthenticationResponse login(
                            final SteppableAuthenticationRequest request) {

                        final SteppableAuthenticationResponse response;

                        if (!request.getStep().isPresent()) {
                            // Authentication step 1
                            final Field description =
                                    Field.builder()
                                            .name("logindescription")
                                            .description("Challenge")
                                            .build();

                            // Challenge which the user is prompted to enter into the card reader
                            description.setValue("123456");

                            final Field input =
                                    Field.builder()
                                            .name("logininput")
                                            .description("Response code, 6 digits")
                                            .build();

                            final AuthenticationResponse payload =
                                    new AuthenticationResponse(Arrays.asList(description, input));

                            response =
                                    SteppableAuthenticationResponse.intermediateResponse(
                                            LoginStep.class, payload);
                        } else {
                            // Authentication step 2
                            final AuthenticationRequest requestPayload =
                                    new AuthenticationRequest(
                                            request.getPayload().getUserInputs(),
                                            new Credentials());
                            final AuthenticationResponse payload =
                                    new LoginStep().respond(requestPayload);

                            response = SteppableAuthenticationResponse.finalResponse(payload);
                        }

                        return response;
                    }
                };

        final SteppableAuthenticationRequest request1 =
                SteppableAuthenticationRequest.initialRequest();

        final SteppableAuthenticationResponse response1 = agent.login(request1);

        Assert.assertTrue(response1.getStep().isPresent());
        Assert.assertEquals(LoginStep.class, response1.getStep().get());
        Assert.assertEquals(2, response1.getPayload().getFields().size());

        // Response code given by the user and their card reader
        final List<String> responseCode = Collections.singletonList("133700");

        final SteppableAuthenticationRequest request2 =
                SteppableAuthenticationRequest.subsequentRequest(
                        response1.getStep().get(), new AuthenticationRequest(responseCode, null));

        final SteppableAuthenticationResponse response2 = agent.login(request2);

        Assert.assertEquals(Optional.empty(), response2.getStep());
        Assert.assertEquals(Collections.emptyList(), response2.getPayload().getFields());
    }
}
