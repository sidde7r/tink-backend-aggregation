package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.session;

import java.util.Optional;

public interface WLSessionHandlerStorage {

    /**
     * Will exist in storage after successful authentication.
     */
    Optional<String> getOptionalWlInstanceId();
}
