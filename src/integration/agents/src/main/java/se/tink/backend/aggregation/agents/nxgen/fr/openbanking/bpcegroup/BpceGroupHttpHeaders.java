package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BpceGroupHttpHeaders {
    SIGNATURE("Signature"),
    X_REQUEST_ID("X-request-id");

    private final String name;
}
