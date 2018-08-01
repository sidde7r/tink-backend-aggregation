package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BelgianIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.i18n.Catalog;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils.createTransferSignature;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils.getFormattedAmount;

public class BelfiusTransferExecutor implements BankTransferExecutor {

    private final BelfiusSessionStorage belfiusSessionStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final CountryDateUtils countryDateUtils;
    private final Catalog catalog;
    private BelfiusApiClient apiClient;

    public BelfiusTransferExecutor(BelfiusApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            BelfiusSessionStorage belfiusSessionStorage, Catalog catalog) {
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.belfiusSessionStorage = belfiusSessionStorage;
        countryDateUtils = CountryDateUtils.getBelgianDateUtils();
        this.supplementalInformationController = supplementalInformationController;
    }

    public static String formatStructuredMessage(String structuredMessage) {
        structuredMessage = structuredMessage.replace("+++", "");
        String[] parts = structuredMessage.split("/");
        String special = Character.toString((char) 92);

        return parts[0] + special + "/" + parts[1] + special + "/" + parts[2];
    }

    @Override
    public void executeTransfer(Transfer transfer) throws TransferExecutionException {
        validateDates(transfer);
        boolean ownAccount = isOwnAccount(transfer.getDestination());

        if (transfer.getMessageType().equals(MessageType.STRUCTURED)) {
            transfer.setDestinationMessage(formatStructuredMessage(transfer.getDestinationMessage()));
        }

        BelfiusPaymentResponse paymentResponse = apiClient
                .executePayment(ownAccount, transfer, createClientSha(transfer),
                        transfer.getMessageType().equals(MessageType.STRUCTURED));

        if (!ownAccount && !containsAccount(((BelgianIdentifier) (transfer.getDestination())).getIban(),
                apiClient.prepareTransfer().getBeneficiaries())) {
            addBeneficiary(transfer, transfer.getMessageType().equals(MessageType.STRUCTURED));
        }

        if (paymentResponse.isDoublePayment()) {
            apiClient.doublePayment();
        }

        if (paymentResponse.requireSign()) {
            signPayments();
        }
    }

    public void signPayments() {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response = "";

        try {
            response = waitForSignCode(transferSignChallenge.getChallenge(), transferSignChallenge.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        SignProtocolResponse signProtocolResponse = apiClient.signTransfer(response);

        if (!signProtocolResponse.signOk()) {
            multiSignTransfer(signProtocolResponse);
        }
    }

    public void checkThrowableErrors(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        if (signProtocolResponse.weeklyCardLimitReached()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        if (signProtocolResponse.invalidBeneficiarySign() || signProtocolResponse.signError()) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    public void multiSignTransfer(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        boolean sucess = false;
        if (signProtocolResponse.signTempError()) {
            try {
                sucess = doubleSignedPayment();
            } catch (SupplementalInfoException e) {
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
        }

        if (!sucess) {
            checkThrowableErrors(signProtocolResponse);
        }
    }

    public TransferExecutionException createFailedTransferException(TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage){
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    public boolean doubleSignedPayment() throws SupplementalInfoException, TransferExecutionException {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response = waitForSignCode(transferSignChallenge.getChallenge(), transferSignChallenge.getSignType());
        SignProtocolResponse signProtocolResponse = apiClient.doubleSignTransfer(response);

        if (signProtocolResponse.signOk()) {
            return true;
        } else if (signProtocolResponse.signError()) {
            signProtocolResponse = apiClient.doubleClickPayment();
            response = waitForSignCode(signProtocolResponse.getChallenge(), signProtocolResponse.getSignType());
            signProtocolResponse = apiClient.doubleSignTransfer(response);
            checkThrowableErrors(signProtocolResponse);
            return signProtocolResponse.signOk();
        }

        return false;
    }

    public boolean containsAccount(String accountNum, List<BeneficiariesContacts> beneficiaries) {
        return beneficiaries.stream()
                .anyMatch(beneficiary -> beneficiary.isAccount(accountNum));
    }

    public void addBeneficiary(Transfer transfer, boolean isStructuredMessage) throws TransferExecutionException {
        String response = "";
        try {
            String name = transfer.getDestination().getName().orElse(null);
            if (name == null) {
                name = addBeneficiaryName();
            }
            SignProtocolResponse signProtocolResponse = apiClient.addBeneficiary(transfer, isStructuredMessage, name);
            response = waitForSignCode(signProtocolResponse.getChallenge(), signProtocolResponse.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        checkThrowableErrors(apiClient.signBeneficiary(response));
    }

    public void validateDates(Transfer transfer) {
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (!countryDateUtils.isBusinessDay(transfer.getDueDate())) {
            transfer.setDueDate(countryDateUtils.getNextBusinessDay(transfer.getDueDate()));
        }
    }

    private boolean isOwnAccount(AccountIdentifier accountIdentifier) {
        BelfiusTransactionalAccountFetcher accountFetcher = new BelfiusTransactionalAccountFetcher(apiClient);
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        return transactionalAccounts.stream()
                .map(TransactionalAccount::getIdentifiers)
                .flatMap(List::stream)
                .anyMatch(identifier -> identifier.equals(accountIdentifier));
    }

    public String createClientSha(Transfer transfer) {
        return createTransferSignature(belfiusSessionStorage.getChallenge(),
                "I" + ((BelgianIdentifier) (transfer.getSource())).getIban(),
                "I" + ((BelgianIdentifier) (transfer.getDestination())).getIban(),
                getFormattedAmount(transfer.getAmount()),
                transfer.getAmount().getCurrency());
    }

    private String addBeneficiaryName() throws SupplementalInfoException {
        String helpText = BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_NAME_HELP_TEXT;
        Field inputField = addBeneficiaryField(helpText);

        return supplementalInformationController.askSupplementalInformation(inputField)
                .get(BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_INP_FIELD);
    }

    private String waitForSignCode(String challenge, String descriptionCode) throws SupplementalInfoException {
        return waitForSupplementalInformation(
                BelfiusConstants.InputFieldConstants.WAIT_FOR_SIGN_CODE_HELP_TEXT_1,
                challenge,
                BelfiusConstants.InputFieldConstants.WAIT_FOR_SIGN_CODE_HELP_TEXT_2,
                descriptionCode);
    }

    private String waitForSupplementalInformation(String helpText, String controlCode, String descriptionCodeHelp,
            String descriptionCode) throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(
                createDescriptionField(
                        BelfiusConstants.InputFieldConstants.CONTROL_CODE_FIELD_DESCRIPTION,
                        helpText,
                        controlCode),
                createDescriptionField(
                        BelfiusConstants.InputFieldConstants.DESCRIPTION_CODE_FIELD_DESCRIPTION,
                        descriptionCodeHelp,
                        descriptionCode),
                createInputField(BelfiusConstants.MultiFactorAuthentication.CODE))
                .get(BelfiusConstants.MultiFactorAuthentication.CODE);
    }

    private Field addBeneficiaryField(String helpText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(BelfiusConstants.InputFieldConstants.BENEFICIARY_FIELD_DESCRIPTION);
        field.setHelpText(helpText);
        field.setHint("name");
        field.setName(BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_INP_FIELD);
        return field;
    }

    private Field createDescriptionField(String descriptionName, String description, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(descriptionName);
        field.setName("description");
        field.setHelpText(description);
        field.setValue(challenge);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String name) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(BelfiusConstants.InputFieldConstants.RESPONSE_CODE_FIELD_DESCRIPTION);
        field.setName(name);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }
}
