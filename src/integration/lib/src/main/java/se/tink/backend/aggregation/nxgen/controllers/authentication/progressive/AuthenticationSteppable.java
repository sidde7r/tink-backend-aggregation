package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive;

import java.util.Optional;

public interface AuthenticationSteppable {
    /**
     * @return The class of the upcoming authentication step to be carried out, or Optional.empty()
     *     if there is none.
     */
    Optional<String> getStepIdentifier();
}
