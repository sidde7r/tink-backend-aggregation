package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.base.Preconditions;
import java.util.Optional;

public final class SteppableAuthenticationResponse implements AuthenticationSteppable {

    private final Optional<String> nextStepIdentifier;
    private final SupplementInformationRequester supplementInformationRequester;
    private final String persistentData;

    private SteppableAuthenticationResponse(
            final String nextStepIdentifier,
            final SupplementInformationRequester response,
            final String persistentData) {
        this.nextStepIdentifier = Optional.of(nextStepIdentifier);
        this.supplementInformationRequester = Preconditions.checkNotNull(response);
        this.persistentData = persistentData;
    }

    private SteppableAuthenticationResponse(final String persistentData) {
        nextStepIdentifier = Optional.empty();
        this.persistentData = persistentData;
        supplementInformationRequester = null;
    }

    @Deprecated
    public static SteppableAuthenticationResponse intermediateResponse(
            final String nextStepIdentifier, final SupplementInformationRequester response) {

        return new SteppableAuthenticationResponse(nextStepIdentifier, response, null);
    }

    public static SteppableAuthenticationResponse intermediateResponse(
            final String nextStepIdentifier,
            final SupplementInformationRequester response,
            final String persistentData) {
        return new SteppableAuthenticationResponse(nextStepIdentifier, response, persistentData);
    }

    public static SteppableAuthenticationResponse finalResponse(final String persistentData) {
        return new SteppableAuthenticationResponse(persistentData);
    }

    @Override
    public Optional<String> getStepIdentifier() {
        return nextStepIdentifier;
    }

    public SupplementInformationRequester getSupplementInformationRequester() {
        return supplementInformationRequester;
    }

    public String getPersistentData() {
        return persistentData;
    }
}
