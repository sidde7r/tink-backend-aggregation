package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import java.util.Collection;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.IcaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.BankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.OwnRecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferRequest;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class IcaBankenBankIdTransferExecutor implements BankTransferExecutor {

	private final IcaBankenApiClient apiClient;
	private final AgentContext context;
	private final TransferMessageFormatter transferMessageFormatter;
	private IcaBankenTransferDestinationFetcher transferDestinationFetcher;
	private OwnAccountsEntity source;
	private RecipientEntity destination;

	public IcaBankenBankIdTransferExecutor(
			IcaBankenApiClient apiClient, AgentContext context, TransferMessageFormatter transferMessageFormatter,
			IcaBankenTransferDestinationFetcher transferDestinationFetcher) {
		this.apiClient = apiClient;
		this.context = context;
		this.transferMessageFormatter = transferMessageFormatter;
		this.transferDestinationFetcher = transferDestinationFetcher;
	}

	@Override
	public void executeTransfer(Transfer transfer) {

		validateNoUnsignedTransfers();

		Collection<OwnAccountsEntity> ownAccounts = apiClient.requestAccountsBody().getAccounts().getOwnAccounts();
		Collection<RecipientEntity> recipientAccounts = apiClient.fetchDestinationAccounts();

		AccountIdentifier transferSource = transfer.getSource();
		AccountIdentifier transferDestination = transfer.getDestination();


		for (OwnAccountsEntity ownAccount : ownAccounts) {

			AccountIdentifier ownIdentifier = ownAccount.generalGetAccountIdentifier();

            if (transferSource.equals(ownIdentifier)){
				source = ownAccount;
			}
			if (transferDestination.equals(ownIdentifier)){
				new OwnRecipientEntity(ownAccount);
            	destination = new OwnRecipientEntity(ownAccount);
			}
		}

		for (RecipientEntity recipientAccount : recipientAccounts) {
			AccountIdentifier recipientIdentifier = recipientAccount.generalGetAccountIdentifier();
			if (transferDestination.equals(recipientIdentifier)){
				destination = recipientAccount;
			}
		}
			makeTransfer(transfer, source, destination);
	}

	private void makeTransfer(Transfer transfer, OwnAccountsEntity fromAccount, RecipientEntity recipientAccount) {
		TransferRequest transferRequest;
		if (transfer.getType().equals(TransferType.PAYMENT)) {

			PaymentRequest paymentRequest = new PaymentRequest();
			paymentRequest.setReferenceType(IcaBankenUtils.getReferenceTypeFor(transfer));
			transferRequest = paymentRequest;
			transferRequest.setMemo(transfer.getSourceMessage());
			transferRequest.setReference(transfer.getDestinationMessage());

		} else {

			transferRequest = new BankTransferRequest();
			TransferMessageFormatter.Messages formattedMessages = transferMessageFormatter
					.getMessages(transfer, recipientAccount.isOwnAccount());
			transferRequest.setMemo(formattedMessages.getSourceMessage());
			transferRequest.setReference(formattedMessages.getDestinationMessage());
		}

		transferRequest.setAmount(transfer.getAmount().getValue());
		transferRequest.setDueDate(IcaBankenUtils.findOrCreateDueDateFor(transfer));
		transferRequest.setFromAccountId(fromAccount.getAccountId());
		transferRequest.setRecipientId(recipientAccount.getRecipientId());
		transferRequest.setRecipientAccountNumber(recipientAccount.getAccountNumber());
		transferRequest.setRecipientType(recipientAccount.getType());
		transferRequest.setType(transfer.getType());

		apiClient.makeTransferRequest(transferRequest);

	}

	public boolean hasUnsignedTransfers() {
		AssignmentsResponse unsignedTransferResponse = apiClient.fetchUnsignedTransfers();

		Boolean valueIs = !unsignedTransferResponse.getBody().getAssignments().isEmpty();
		return valueIs;
	}

	private void validateNoUnsignedTransfers() {
		if (hasUnsignedTransfers()) {
			throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
					.setEndUserMessage(context.getCatalog()
							.getString(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
					.build();
		}
	}

	public String init() {

		return apiClient
				.initBankIdTransfer()
				.getBody()
				.getRequestId();

	}

	public BankIdStatus collect(String reference) {

		BankIdStatus response = apiClient
				.sign(reference)
				.getBody()
				.getBankIdStatus();

		return response;
	}

	public SignedAssignmentList signAssignment(String requestId){
		SignedAssignmentList assignmentList = apiClient.getSignedAssignmentsList(requestId);
		return assignmentList;
	}
}
