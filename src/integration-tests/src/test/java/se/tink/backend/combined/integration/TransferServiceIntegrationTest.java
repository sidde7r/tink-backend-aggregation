package se.tink.backend.combined.integration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.api.AccountService;
import se.tink.backend.api.TransferService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Amount;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferEvent;

/**
 * TODO this is a unit test
 */
public class TransferServiceIntegrationTest extends AbstractServiceIntegrationTest {

    /**
     * Verifies that transfer events (auditing) are created when we create new transfers
     */
    @Ignore
    @Test
    public void verifyThatEventsAreCreatedForNewTransfers() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("201212121215");

        SignableOperation signableOperation = createTransfer(user);

        TransferEventRepository repository = serviceContext.getRepository(TransferEventRepository.class);

        // Verify that the created event is logged
        List<TransferEvent> events = repository.findAllByUserIdAndTransferId(signableOperation.getUserId(), signableOperation.getUnderlyingId());

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(SignableOperationStatuses.CREATED, events.get(0).getStatus());

        deleteUser(user);

    }

    /**
     * Verifies that transfer events (auditing) are created when we update a transfer through system
     */
    @Ignore
    @Test
    public void verifyThatEventsAreCreatedForUpdates() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData("201212121212");

        SignableOperation signableOperation = createTransfer(user);

        TransferEventRepository repository = serviceContext.getRepository(TransferEventRepository.class);

        List<TransferEvent> eventsBefore = repository.findAllByUserIdAndTransferId(signableOperation.getUserId(), signableOperation.getUnderlyingId());

        signableOperation.setStatus(SignableOperationStatuses.FAILED);

        // Update the transfer
        systemServiceFactory.getUpdateService().updateSignableOperation(signableOperation);

        Thread.sleep(2000);

        List<TransferEvent> eventsAfter = repository.findAllByUserIdAndTransferId(signableOperation.getUserId(), signableOperation.getUnderlyingId());

        // One new event
        Assert.assertTrue(eventsAfter.size() - eventsBefore.size() > 1);

        Optional<TransferEvent> failedEvent = eventsAfter.stream()
                .filter(transferEvent -> transferEvent.getStatus() == SignableOperationStatuses.FAILED).findFirst();

        Assert.assertTrue(failedEvent.isPresent());

        deleteUser(user);
    }

    @Test
    public void correctSaveAndReadAmount() {
        double amount = 15;

        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(amount));
        transfer.setUserId(UUIDUtils.fromTinkUUID("07da44eeae8d448b8b67bdf5e0488832"));

        TransferRepository transferRepository = serviceContext.getRepository(TransferRepository.class);

        UUID id = transferRepository.save(transfer).getId();
        Transfer savedTransfer = transferRepository.findOneByUserIdAndId(transfer.getUserId(), id);

        Assert.assertEquals(amount, savedTransfer.getAmount().getValue(), 0.00);
    }

    private SignableOperation createTransfer(User user) {

        AccountService accountService = serviceFactory.getAccountService();
        TransferService transferService = serviceFactory.getTransferService();

        List<Account> accounts = accountService.listAccounts(user).getAccounts();

        Assert.assertTrue("We need at least two accounts", accounts.size() >= 2);

        AccountIdentifier identifier1 = accounts.get(0).getIdentifier(AccountIdentifier.Type.SE);
        AccountIdentifier identifier2 = accounts.get(1).getIdentifier(AccountIdentifier.Type.SE);

        Assert.assertNotNull("No identifier found", identifier1);
        Assert.assertNotNull("No identifier found", identifier2);

        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(100D));
        transfer.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        transfer.setDestination(identifier1);
        transfer.setSource(identifier2);

        SignableOperation signableOperation = transferService.createTransfer(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), transfer);

        Assert.assertNotNull("Transfer was not created", signableOperation);

        // Verify that the transfer is saved
        Assert.assertNotNull(transferService.get(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), UUIDUtils.toTinkUUID(signableOperation.getUnderlyingId())));

        return signableOperation;
    }
}
