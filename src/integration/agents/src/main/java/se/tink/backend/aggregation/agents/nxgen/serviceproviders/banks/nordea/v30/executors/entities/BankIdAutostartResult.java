package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;

@Getter
@Builder
public class BankIdAutostartResult {
    private BankIdStatus status;
    private String code;
}
