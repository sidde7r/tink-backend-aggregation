package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ValidationType {
    PASSWORD("PASSWORD"),
    SMS("SMS");

    private String name;
}
