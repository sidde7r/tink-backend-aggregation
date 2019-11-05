package se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;

public class BankIdSigningController<T> implements Signer<T> {
    private static final int MAX_ATTEMPTS = 90;

    private static final AggregationLogger log =
            new AggregationLogger(BankIdSigningController.class);
    private final BankIdSigner<T> signer;
    private final SupplementalRequester supplementalRequester;

    public BankIdSigningController(
            SupplementalRequester supplementalRequester, BankIdSigner signer) {
        this.signer = Preconditions.checkNotNull(signer);
        this.supplementalRequester = Preconditions.checkNotNull(supplementalRequester);
    }

    public void sign(T toSign) throws AuthenticationException {
        supplementalRequester.openBankId(signer.getAutostartToken().orElse(null), false);
        poll(toSign);
    }

    private void poll(T toSign) throws AuthenticationException {
        BankIdStatus status = null;

        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            status = signer.collect(toSign);

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    log.info("Waiting for BankID");
                    break;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case NO_CLIENT:
                    throw BankIdError.NO_CLIENT.exception();
                case TIMEOUT:
                    throw BankIdError.TIMEOUT.exception();
                case INTERRUPTED:
                    throw BankIdError.INTERRUPTED.exception();
                default:
                    log.warn(String.format("Unknown BankIdStatus (%s)", status));
                    throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        log.info(String.format("BankID timed out internally, last status: %s", status));
        throw BankIdError.TIMEOUT.exception();
    }
}
