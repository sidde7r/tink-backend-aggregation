package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

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
                        return SteppableAuthenticationResponse.finalResponse();
                    }
                };

        final SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));

        final SteppableAuthenticationResponse response = agent.login(request);

        Assert.assertEquals(Optional.empty(), response.getStepIdentifier());
        Assert.assertNull(response.getSupplementInformationRequester());
    }

    @Test
    public void testTwoSteps() throws Exception {

        class LoginStep implements AuthenticationStep {

            @Override
            public AuthenticationStepResponse execute(final AuthenticationRequest request) {

                // Unless the following is true, the authenticator would throw INCORRECT_CREDENTIALS
                Assert.assertEquals(1, request.getUserInputsAsList().size());
                Assert.assertEquals("133700", request.getUserInputsAsList().get(0));

                return AuthenticationStepResponse.executeNextStep();
            }
        }

        final ProgressiveAuthAgent agent =
                new AgentStub() {
                    @Override
                    public SteppableAuthenticationResponse login(
                            final SteppableAuthenticationRequest request) {

                        final SteppableAuthenticationResponse response;

                        if (!request.getStepIdentifier().isPresent()) {
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

                            final SupplementInformationRequester payload =
                                    new SupplementInformationRequester.Builder()
                                            .withFields(Arrays.asList(description, input))
                                            .build();

                            response =
                                    SteppableAuthenticationResponse.intermediateResponse(
                                            LoginStep.class.getName(), payload);
                        } else {
                            // Authentication step 2
                            final AuthenticationRequest requestPayload =
                                    new AuthenticationRequest(Mockito.mock(Credentials.class))
                                            .withUserInputs(request.getPayload().getUserInputs())
                                            .withCredentials(new Credentials());

                            response = SteppableAuthenticationResponse.finalResponse();
                        }

                        return response;
                    }
                };

        final SteppableAuthenticationRequest request1 =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));

        final SteppableAuthenticationResponse response1 = agent.login(request1);

        Assert.assertTrue(response1.getStepIdentifier().isPresent());
        Assert.assertEquals(LoginStep.class.getName(), response1.getStepIdentifier().get());
        Assert.assertTrue(response1.getSupplementInformationRequester().getFields().isPresent());
        Assert.assertEquals(
                2, response1.getSupplementInformationRequester().getFields().get().size());

        // Response code given by the user and their card reader
        Map<String, String> userInputs = new HashMap<>();
        userInputs.put("RESPONSE_CODE", "133700");

        final SteppableAuthenticationRequest request2 =
                SteppableAuthenticationRequest.subsequentRequest(
                        response1.getStepIdentifier().get(),
                        new AuthenticationRequest(Mockito.mock(Credentials.class))
                                .withUserInputs(userInputs));
        final SteppableAuthenticationResponse response2 = agent.login(request2);

        Assert.assertEquals(Optional.empty(), response2.getStepIdentifier());
        Assert.assertNull(response2.getSupplementInformationRequester());
    }
}
