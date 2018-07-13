package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentList;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.UpdatePaymentExecutor;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;

public class IcaBankenBankIdTransferController extends TransferController {
    private static final int MAX_ATTEMPTS = 90;
    private final IcaBankenBankIdTransferExecutor executor;
    private final AgentContext context;

    @Override
    public void execute(Transfer transfer) {

        executor.executeTransfer(transfer);
        validateAndSignTransfer();
    }

    public IcaBankenBankIdTransferController(AgentContext context, IcaBankenBankIdTransferExecutor transferExecutor,
            PaymentExecutor paymentExecutor, ApproveEInvoiceExecutor approveEInvoiceExecutor, UpdatePaymentExecutor
            updatePaymentExecutor) {
        super(paymentExecutor, transferExecutor, approveEInvoiceExecutor, updatePaymentExecutor);
        this.executor = Preconditions.checkNotNull(transferExecutor);
        this.context = Preconditions.checkNotNull(context);
    }

    private void validateAndSignTransfer() {
        if (!executor.hasUnsignedTransfers()) {
            return;
        }

        execute();
    }

    public void execute() {

        String reference = executor.init();

        context.openBankId(null, false);

        try {
            poll(reference);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        SignedAssignmentList assignments = executor.signAssignment(reference);

        if (assignments.containRejected()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Transfer rejected by ICA")
                    .setEndUserMessage("Transfer failed, WHY??")
                    .build();
        }
    }

    private void poll(String reference) throws AuthenticationException {
        BankIdStatus status;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            status = executor.collect(reference);

            switch (status) {
            case DONE:
                return;
            case WAITING:
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
                throw BankIdError.UNKNOWN.exception();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }
}
