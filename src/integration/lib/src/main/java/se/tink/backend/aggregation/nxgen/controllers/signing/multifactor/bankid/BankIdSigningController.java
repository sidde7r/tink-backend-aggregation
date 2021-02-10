package se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class BankIdSigningController<T> implements Signer<T> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MAX_ATTEMPTS = 90;

    private final BankIdSigner<T> signer;
    private final SupplementalInformationController supplementalInformationController;

    public BankIdSigningController(
            SupplementalInformationController supplementalInformationController,
            BankIdSigner signer) {
        this.signer = Preconditions.checkNotNull(signer);
        this.supplementalInformationController =
                Preconditions.checkNotNull(supplementalInformationController);
    }

    public void sign(T toSign) throws AuthenticationException {
        supplementalInformationController.openMobileBankIdAsync(
                signer.getAutostartToken().orElse(null));
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
                    logger.info("Waiting for BankID");
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
                    logger.warn(String.format("Unknown BankIdStatus (%s)", status));
                    throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        logger.info(String.format("BankID timed out internally, last status: %s", status));
        throw BankIdError.TIMEOUT.exception();
    }
}
