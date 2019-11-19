package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public abstract class StatelessProgressiveAuthenticator<T> implements ProgressiveAuthenticator {

    private Class<T> persistentDataClass;

    public StatelessProgressiveAuthenticator(Class<T> persistentDataClass) {
        this.persistentDataClass = persistentDataClass;
    }

    public SteppableAuthenticationResponse processAuthentication(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        T persistentData = deserializePersistentData(request.getPersistentData());
        List<? extends AuthenticationStep> stepsToProcess = findAndExtractStepsToProcess(request);
        for (AuthenticationStep step : stepsToProcess) {
            Optional<SupplementInformationRequester> response =
                    step.execute(request.getPayload(), persistentData);
            if (response.isPresent()) {
                return SteppableAuthenticationResponse.intermediateResponse(
                        step.getIdentifier(),
                        response.get(),
                        serializePersistentData(persistentData));
            }
        }
        return SteppableAuthenticationResponse.finalResponse(
                serializePersistentData(persistentData));
    }

    private List<? extends AuthenticationStep> findAndExtractStepsToProcess(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        List<? extends AuthenticationStep> authSteps = Lists.newArrayList(authenticationSteps());
        if (request.getStepIdentifier().isPresent()) {
            for (int i = 0; i < authSteps.size(); i++) {
                if (authSteps.get(i).getIdentifier().equals(request.getStepIdentifier().get())) {
                    return authSteps.subList(i, authSteps.size());
                }
            }
            throw new IllegalStateException(
                    "Step with identifier [" + request.getStepIdentifier() + "] doesn't exist");
        } else {
            return authSteps;
        }
    }

    private T deserializePersistentData(String persistentData) {
        try {

            if (persistentData == null) {
                return persistentDataClass.newInstance();
            }
            Gson gson = new Gson();
            return gson.fromJson(persistentData, persistentDataClass);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(
                    "Error occur during JSON persistent data deserialization");
        }
    }

    private String serializePersistentData(T persistentData) {
        Gson gson = new Gson();
        return gson.toJson(persistentData);
    }
}
