package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsentType {
    ACCOUNT("acc"),
    BALANCE_TRANSACTION("trans");

    private final String code;
}
