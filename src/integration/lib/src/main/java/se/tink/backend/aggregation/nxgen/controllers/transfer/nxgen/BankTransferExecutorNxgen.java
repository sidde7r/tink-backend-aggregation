package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.Beneficiary;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.OutboxItem;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferDestination;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferSource;
import se.tink.libraries.account.AccountIdentifier;

public interface BankTransferExecutorNxgen {

    /**
     * Initialize and prepare the agent for doing transfers. This could for example include doing
     * one request to fetch source accounts, destinations and beneficiaries.
     */
    void initialize();

    /**
     * Return true if the outbox is empty or false if it contains payments that haven't been signed.
     *
     * @return true if the outbox is empty.
     */
    boolean isOutboxEmpty();

    /** Add a new item to the outbox. */
    void addToOutbox(OutboxItem item);

    /** Remove all items from the outbox. */
    void cleanOutbox();

    /** Sign all items in the outbox. */
    void signOutbox();

    /** Return a collection of the beneficiaries that are available. */
    Collection<Beneficiary> getBeneficiaries();

    /**
     * Add a new beneficiary. This may also include signing if that's required by the agent.
     *
     * @param name name of the beneficiary / destination
     * @param identifier account identifier of the beneficiary / destination
     * @return the newly added beneficiary
     */
    Beneficiary addBeneficiary(String name, AccountIdentifier identifier);

    /** Return a collection of the source accounts that are available. */
    Collection<TransferSource> getSourceAccounts();

    /** Return a collection of internal transfer destinations that are available. */
    Collection<TransferDestination> getInternalTransferDestinations();
}
