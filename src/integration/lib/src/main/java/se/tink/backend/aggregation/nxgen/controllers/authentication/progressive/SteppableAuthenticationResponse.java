package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public final class SteppableAuthenticationResponse implements AuthenticationSteppable {

    private final Optional<String> nextStepIdentifier;
    private final SupplementInformationRequester supplementInformationRequester;

    private SteppableAuthenticationResponse(
            final String nextStepIdentifier, final SupplementInformationRequester response) {
        this.nextStepIdentifier = Optional.of(nextStepIdentifier);
        this.supplementInformationRequester = Preconditions.checkNotNull(response);
    }

    private SteppableAuthenticationResponse() {
        nextStepIdentifier = Optional.empty();
        supplementInformationRequester = null;
    }

    public static SteppableAuthenticationResponse intermediateResponse(
            final String nextStepIdentifier, final SupplementInformationRequester response) {

        return new SteppableAuthenticationResponse(nextStepIdentifier, response);
    }

    public static SteppableAuthenticationResponse finalResponse() {
        return new SteppableAuthenticationResponse();
    }

    @Override
    public Optional<String> getStepIdentifier() {
        return nextStepIdentifier;
    }

    public SupplementInformationRequester getSupplementInformationRequester() {
        return supplementInformationRequester;
    }
}
