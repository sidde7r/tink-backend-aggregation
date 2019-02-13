package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.libraries.transfer.rpc.Transfer;

public class ValidateTrustedTransferBody extends MultivaluedMapImpl {

    public ValidateTrustedTransferBody(
            Transfer transfer, AccountEntity sourceAccount, String destinationAccountNumber) {

        add(
                IngConstants.Transfers.AMOUNT,
                IngTransferHelper.formatUnsignedTransferAmount(transfer.getAmount().getValue()));
        add(
                IngConstants.Transfers.P_ACCOUNT,
                Preconditions.checkNotNull(sourceAccount.getIbanNumber()));
        add(
                IngConstants.Transfers.P_ACCOUNT_313,
                Preconditions.checkNotNull(sourceAccount.getAccount313()));
        add(IngConstants.Transfers.P_NAME, Preconditions.checkNotNull(sourceAccount.getName()));
        add(
                IngConstants.Transfers.P_ADDRESS,
                Preconditions.checkNotNull(sourceAccount.getAddress()));
        add(
                IngConstants.Transfers.ValuePairs.P_COUNTRY.getKey(),
                IngConstants.Transfers.ValuePairs.P_COUNTRY.getValue());
        add(IngConstants.Transfers.P_CITY, Preconditions.checkNotNull(sourceAccount.getCity()));
        add(IngConstants.Transfers.B_ACCOUNT, Preconditions.checkNotNull(destinationAccountNumber));
        add(IngConstants.Transfers.CURRENCY, transfer.getAmount().getCurrency());
        add(
                IngConstants.Transfers.MEMO_DATE,
                IngTransferHelper.formatTransferDueDate(transfer.getDueDate()));
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
        IngTransferHelper.addDestinationMessageByMessageType(
                this, transfer.getMessageType(), transfer.getDestinationMessage());
    }
}
