package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class RequestBasedMultiStepProcessTest {

    static class TestProcess extends RequestBasedMultiStepsProcess {

        private String executedStep;

        @Override
        protected void registerSteps() {
            registerInitialStep(
                    new UserInteractionStep() {

                        @Override
                        public String identifier() {
                            return "step0";
                        }

                        @Override
                        public SteppableAuthenticationResponse execute(
                                SteppableAuthenticationRequest request) {
                            executedStep = "step0";
                            return SteppableAuthenticationResponse.intermediateResponse(
                                    "step1", SupplementInformationRequester.empty());
                        }
                    });
            registerSingleStep(
                    new UserInteractionStep() {

                        @Override
                        public String identifier() {
                            return "step1";
                        }

                        @Override
                        public SteppableAuthenticationResponse execute(
                                SteppableAuthenticationRequest request) {
                            executedStep = "step1";
                            return SteppableAuthenticationResponse.intermediateResponse(
                                    "step2", SupplementInformationRequester.empty());
                        }
                    });
            registerSingleStep(
                    new UserInteractionStep() {

                        @Override
                        public String identifier() {
                            return "step2";
                        }

                        @Override
                        public SteppableAuthenticationResponse execute(
                                SteppableAuthenticationRequest request) {
                            executedStep = "step2";
                            return SteppableAuthenticationResponse.finalResponse();
                        }
                    });
        }
    }

    @Test
    public void executeShouldExecuteInitialStepLogicWhenFirstRequest() throws LoginException {
        // given
        TestProcess sut = new TestProcess();
        sut.registerSteps();

        // when
        SteppableAuthenticationResponse response =
                sut.execute(SteppableAuthenticationRequest.initialRequest(null));

        // then
        Assertions.assertThat(response.getStepIdentifier()).isEqualTo(Optional.of("step1"));
        Assertions.assertThat(sut.executedStep).isEqualTo("step0");
    }

    @Test
    public void
            executeShouldExecuteStepLogicAndMoveFromOneStepToAnotherAndPrepareProperResponseWhenRequestWithNextStepIdentifier()
                    throws LoginException {
        // given
        TestProcess sut = new TestProcess();
        sut.registerSteps();

        // when
        SteppableAuthenticationResponse response =
                sut.execute(
                        SteppableAuthenticationRequest.subsequentRequest(
                                "step1", new AuthenticationRequest(null)));

        // then
        Assertions.assertThat(response.getStepIdentifier()).isEqualTo(Optional.of("step2"));
        Assertions.assertThat(sut.executedStep).isEqualTo("step1");
    }

    @Test
    public void
            executeShouldExecuteLastStepLogicAndPrepareProperResponseWhenRequestWithLastStepIdentifier()
                    throws LoginException {
        // given
        TestProcess sut = new TestProcess();
        sut.registerSteps();

        // when
        SteppableAuthenticationResponse response =
                sut.execute(
                        SteppableAuthenticationRequest.subsequentRequest(
                                "step2", new AuthenticationRequest(null)));

        // then
        Assertions.assertThat(response.getStepIdentifier()).isEqualTo(Optional.empty());
        Assertions.assertThat(sut.executedStep).isEqualTo("step2");
    }
}
