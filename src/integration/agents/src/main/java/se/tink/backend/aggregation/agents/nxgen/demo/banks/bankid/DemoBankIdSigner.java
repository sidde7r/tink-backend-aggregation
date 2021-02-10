package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;

public class DemoBankIdSigner implements BankIdSigner {
    private int attempt = 0;
    private static final Random RANDOM = new SecureRandom();

    @Override
    public BankIdStatus collect(Object toCollect) throws AuthenticationException {
        BankIdStatus status;
        if (attempt > 3) {
            status = BankIdStatus.DONE;
        } else {
            status = BankIdStatus.WAITING;
        }

        attempt++;
        return status;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.of(AutostartTokenGenerator.generateFrom(RANDOM));
    }
}
