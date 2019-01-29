package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InitialTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MakeTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SavedRecipientEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.TransferEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferClient extends SBABClient {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    private static final AggregationLogger log = new AggregationLogger(TransferClient.class);

    private static final String TRANSFER_PAGE_URL = SECURE_BASE_URL + "/privat/ny_overforing";

    private final Catalog catalog;
    private final TransferMessageFormatter messageFormatter;

    public TransferClient(
            Client client, Credentials credentials, Catalog catalog, String userAgent) {
        super(client, credentials, userAgent);
        this.messageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        TransferMessageLengthConfig.createWithMaxLength(30, 12, 12),
                        new StringNormalizerSwedish("!+%\"/?,.§\\-"));
        this.catalog = catalog;
    }

    public InitialTransferResponse initiateProcess() throws Exception {
        Document page = getJsoupDocument(TRANSFER_PAGE_URL);
        Element transferForm = page.select("form[id=overforingForm]").first();
        Element newRecipientForm = page.select("form[id=sparaEjMottagare_overforingForm]").first();

        InitialTransferResponse initialResponse = new InitialTransferResponse();
        initialResponse.setStrutsTokenName(
                transferForm.select("input[name=struts.token.name]").val());
        initialResponse.setToken(transferForm.select("input[name=token]").val());
        initialResponse.setPostUrl(
                SECURE_BASE_URL + transferForm.attr("action").replace(" ", "%20"));
        initialResponse.setSaveRecipientStrutsTokenName(
                newRecipientForm.select("input[name=struts.token.name]").val());
        initialResponse.setSaveRecipientToken(newRecipientForm.select("input[name=token]").val());
        initialResponse.setSaveRecipientPostUrl(
                SECURE_BASE_URL + newRecipientForm.attr("action").replace(" ", "%20"));
        initialResponse.setValidRecipients(getValidRecipients(page));
        initialResponse.setValidSourceAccountNumbers(getValidSourceAccountNumbers(page));

        return initialResponse;
    }

    public MakeTransferResponse makeTransfer(
            Transfer transfer,
            Optional<SavedRecipientEntity> savedRecipient,
            InitialTransferResponse initialResponse)
            throws Exception {
        boolean isBetweenUserAccounts =
                savedRecipient.isPresent() && savedRecipient.get().isUserAccount();

        // Create a transfer object with the values that will actually be sent to SBAB.
        TransferMessageFormatter.Messages messages =
                messageFormatter.getMessages(transfer, isBetweenUserAccounts);
        TransferEntity transferEntity = TransferEntity.create(transfer, messages);

        if (!savedRecipient.isPresent()) {
            validateNewRecipient(transferEntity, initialResponse);
        }

        MultivaluedMapImpl requestBody =
                createRequestBody(transferEntity, savedRecipient, initialResponse);

        ClientResponse transferResponse =
                createFormEncodedHtmlRequest(initialResponse.getPostUrl())
                        .header("Referer", TRANSFER_PAGE_URL)
                        .post(ClientResponse.class, requestBody);

        String redirectUrl = getRedirectUrl(transferResponse, SECURE_BASE_URL);
        Document resultPage = getJsoupDocumentWithReferer(redirectUrl, TRANSFER_PAGE_URL);
        throwIfErrorsArePresent(resultPage);

        MakeTransferResponse makeTransferResponse = new MakeTransferResponse();
        makeTransferResponse.setId(findTransferIdToAccept(transferEntity, resultPage));
        Element acceptTransferTable =
                resultPage.select("form[id=godkannvaldaOverforingarForm]").first();
        makeTransferResponse.setAcceptUrl(SECURE_BASE_URL + acceptTransferTable.attr("action"));
        Element deleteTransferTable =
                resultPage.select("form[id=tabortValdaOverforingarForm]").first();
        makeTransferResponse.setDeleteUrl(SECURE_BASE_URL + deleteTransferTable.attr("action"));
        makeTransferResponse.setStrutsTokenName(
                acceptTransferTable.select("input[name=struts.token.name]").val());
        makeTransferResponse.setToken(acceptTransferTable.select("input[name=token]").val());
        makeTransferResponse.setReferer(redirectUrl);
        makeTransferResponse.setIsBetweenUserAccounts(isBetweenUserAccounts);
        makeTransferResponse.setTransferEntity(transferEntity);

        return makeTransferResponse;
    }

    private void validateNewRecipient(
            TransferEntity transferEntity, InitialTransferResponse initialResponse)
            throws Exception {
        MultivaluedMapImpl saveRecipientBody =
                createSaveRecipientRequestBody(transferEntity, initialResponse);

        ClientResponse response =
                createFormEncodedHtmlRequest(initialResponse.getSaveRecipientPostUrl())
                        .header("Referer", TRANSFER_PAGE_URL)
                        .post(ClientResponse.class, saveRecipientBody);
        String redirectUrl = getRedirectUrl(response, SECURE_BASE_URL);

        Document transferPage = getJsoupDocumentWithReferer(redirectUrl, TRANSFER_PAGE_URL);
        String newToken =
                transferPage
                        .select("form[id=overforingForm]")
                        .first()
                        .select("input[name=token]")
                        .val();
        initialResponse.setToken(newToken);
        checkIfNewRecipientError(transferPage);
    }

    private void checkIfNewRecipientError(Document transferPage) {
        Element potentialError = transferPage.select("div.ny-mottagare div.error").first();

        if (potentialError != null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            "Could not create new recipient. Error message: "
                                    + potentialError.text())
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.NEW_RECIPIENT_FAILED))
                    .build();
        }
    }

    private MultivaluedMapImpl createSaveRecipientRequestBody(
            TransferEntity transferEntity, InitialTransferResponse initialResponse) {
        MultivaluedMapImpl saveRecipientBody = new MultivaluedMapImpl();

        saveRecipientBody.add(
                "nyOverforing.bankkonto.kontonummer", transferEntity.getDestinationAccountNumber());
        saveRecipientBody.add("nyOverforing.mottagarNamn", "");
        saveRecipientBody.add("nyOverforing.sparaOverforingsmottagare", "false");
        saveRecipientBody.add("nyOverforing.overforingBefintligMottagare", "-1");
        saveRecipientBody.add(
                "nyOverforing.franKontonummer", transferEntity.getFromAccountNumber());
        saveRecipientBody.add("nyOverforing.datumBetalning", "");
        saveRecipientBody.add("nyOverforing.formattedBelopp", "");
        saveRecipientBody.add("nyOverforing.meddelandeTillMottagare", "");
        saveRecipientBody.add("nyOverforing.egenNotering", "");
        saveRecipientBody.add("__checkbox_nyOverforing.staendeOverforing", "true");
        saveRecipientBody.add(
                "struts.token.name", initialResponse.getSaveRecipientStrutsTokenName());
        saveRecipientBody.add("token", initialResponse.getSaveRecipientToken());

        return saveRecipientBody;
    }

    public void acceptTransfer(MakeTransferResponse makeTransferResponse) throws Exception {
        String redirectUrl = accept(makeTransferResponse);
        getJsoupDocumentWithReferer(redirectUrl, makeTransferResponse.getReferer());
        checkTransferSuccess(makeTransferResponse, false);
    }

    public void deleteTransfer(MakeTransferResponse makeResponse) throws Exception {
        Document transferPage = getJsoupDocument(TRANSFER_PAGE_URL);
        String newToken =
                transferPage
                        .select("form[id=tabortValdaOverforingarForm]")
                        .first()
                        .select("input[name=token]")
                        .val();
        makeResponse.setToken(newToken);

        String redirectUrl = delete(makeResponse);
        getJsoupDocumentWithReferer(redirectUrl, makeResponse.getReferer());
        String id = makeResponse.getId();

        if (transferIsInInbox(id)) {
            log.error("Failed to delete unsigned transfer from inbox with id = " + id);
        } else {
            log.info("Successfully deleted unsigned transfer from inbox");
        }
    }

    public SignFormRequestBody initiateSignProcess(MakeTransferResponse makeTransferResponse)
            throws Exception {
        String redirectUrl = accept(makeTransferResponse);
        Document signPage =
                getJsoupDocumentWithReferer(redirectUrl, makeTransferResponse.getReferer());
        Element signForm = signPage.select("form[id=signFormNexus]").first();

        SignFormRequestBody signFormRequestBody = SignFormRequestBody.from(signForm);
        signFormRequestBody.setReferer(redirectUrl);

        return signFormRequestBody;
    }

    private String accept(MakeTransferResponse makeTransferResponse) throws Exception {
        return acceptOrDelete(makeTransferResponse.getAcceptUrl(), makeTransferResponse);
    }

    private String delete(MakeTransferResponse makeTransferResponse) throws Exception {
        return acceptOrDelete(makeTransferResponse.getDeleteUrl(), makeTransferResponse);
    }

    private String acceptOrDelete(String url, MakeTransferResponse makeTransferResponse)
            throws Exception {
        MultivaluedMapImpl selectCheckboxBody = createSelectCheckboxBody(makeTransferResponse);

        ClientResponse response =
                createFormEncodedHtmlRequest(url)
                        .header("Referer", makeTransferResponse.getReferer())
                        .post(ClientResponse.class, selectCheckboxBody);

        return getRedirectUrl(response, SECURE_BASE_URL);
    }

    public List<SavedRecipientEntity> getValidRecipients() throws Exception {
        return getValidRecipients(getJsoupDocument(TRANSFER_PAGE_URL));
    }

    private List<SavedRecipientEntity> getValidRecipients(Document transferPage) {
        List<SavedRecipientEntity> savedRecipients = Lists.newArrayList();

        // Get the valid recipients for a transfer, including both own accounts and other saved
        // destinations.
        savedRecipients.addAll(getSavedRecipients(transferPage, true));
        savedRecipients.addAll(getSavedRecipients(transferPage, false));

        return savedRecipients;
    }

    private List<SavedRecipientEntity> getSavedRecipients(
            Document transferPage, boolean chooseUserOwned) {
        Elements recipientElements;

        if (chooseUserOwned) {
            recipientElements = transferPage.select("optgroup[id=mottagareSparkonton] > option");
        } else {
            recipientElements = transferPage.select("optgroup[id=mottagareSparadekonton] > option");
        }

        List<SavedRecipientEntity> recipients = Lists.newArrayList();

        for (Element recipient : recipientElements) {
            Optional<SavedRecipientEntity> recipientEntity =
                    SavedRecipientEntity.createFromString(recipient.val());
            if (recipientEntity.isPresent()) {
                recipientEntity.get().setIsUserAccount(chooseUserOwned);
                recipients.add(recipientEntity.get());
            } else {
                log.error(
                        "Could not create recipient entity from string value ("
                                + recipient.val()
                                + "). New format?");
            }
        }

        return recipients;
    }

    private List<String> getValidSourceAccountNumbers(Document transferPage) {
        List<String> validSourceAccountNumbers = Lists.newArrayList();

        Elements sourceAccountElements =
                transferPage.select("select[id=nyOverforing.belastningskonto] > optgroup > option");

        for (Element sourceAccountElement : sourceAccountElements) {
            validSourceAccountNumbers.add(sourceAccountElement.val());
        }

        return validSourceAccountNumbers;
    }

    public Optional<SavedRecipientEntity> tryFindRecipient(
            Transfer transfer, InitialTransferResponse initialResponse) {
        final AccountIdentifier destinationIdentifier = transfer.getDestination();
        List<SavedRecipientEntity> savedRecipients = initialResponse.getValidRecipients();

        return savedRecipients.stream()
                .filter(
                        savedRecipient -> {
                            AccountIdentifier savedIdentifier =
                                    new SwedishIdentifier(savedRecipient.getAccountNumber());
                            return Objects.equal(destinationIdentifier, savedIdentifier);
                        })
                .findFirst();
    }

    public void checkTransferSuccess(MakeTransferResponse response, boolean isExternal)
            throws Exception {
        try {
            if (transferIsInInbox(response.getId())) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage("Tried to execute transfer, but transfer remains in inbox")
                        .build();
            }
        } catch (TransferExecutionException e) {
            throw e;
        } catch (Exception e) {
            if (!isExternal) {
                throw e;
            }

            if (!transferIsUpcoming(response.getTransferEntity())) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage(
                                "Tried to execute transfer, but failed to check the inbox and couldn't find transfer "
                                        + "among upcoming transfers either")
                        .build();
            }
        }
    }

    private boolean transferIsInInbox(String id) throws Exception {
        Document acceptPage = getJsoupDocument(TRANSFER_PAGE_URL);
        Element acceptTransferTable = acceptPage.select("table.attgodkannaoversikt").first();

        if (acceptTransferTable == null) {
            return false;
        }

        Elements checkBoxes = acceptTransferTable.select("input[name=checkboxar]");

        // Look for the transfer id in the 'transfers to accept' table.
        for (Element checkbox : checkBoxes) {
            if (Objects.equal(checkbox.attr("id"), id)) {
                return true;
            }
        }

        return false;
    }

    private boolean transferIsUpcoming(TransferEntity executedEntity) throws Exception {
        Document acceptPage = getJsoupDocument(TRANSFER_PAGE_URL);
        List<TransferEntity> upcomingTransfers =
                TransferClientParser.parseUpcomingTransfers(
                        acceptPage.select("table.kommandeoversikt").first());

        for (TransferEntity transferEntity : upcomingTransfers) {
            if (transferEntity.equals(executedEntity)) {
                return true;
            }
        }

        return false;
    }

    private MultivaluedMapImpl createSelectCheckboxBody(MakeTransferResponse makeTransferResponse) {
        MultivaluedMapImpl acceptTransferBody = new MultivaluedMapImpl();

        acceptTransferBody.add("valdaCheckboxar", makeTransferResponse.getId());
        acceptTransferBody.add("struts.token.name", makeTransferResponse.getStrutsTokenName());
        acceptTransferBody.add("token", makeTransferResponse.getToken());

        return acceptTransferBody;
    }

    private void throwIfErrorsArePresent(Document resultPage) {
        Element error = resultPage.select("form[id=overforingForm] div[class=error]").first();

        if (error != null && !elementIsHidden(error)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(error.text())
                    .setEndUserMessage(error.text())
                    .build();
        }
    }

    private boolean elementIsHidden(Element element) {
        String style = element.attr("style");
        return style.contains("display: none");
    }

    public Optional<String> tryFindSourceAccount(
            Transfer transfer, InitialTransferResponse initialResponse) {
        final String sourceIdentifier = transfer.getSource().getIdentifier(DEFAULT_FORMATTER);
        List<String> validSourceAccountNumbers = initialResponse.getValidSourceAccountNumbers();

        return validSourceAccountNumbers.stream()
                .filter(
                        sourceAccountNumber ->
                                (Objects.equal(sourceIdentifier, sourceAccountNumber)))
                .findFirst();
    }

    private MultivaluedMapImpl createRequestBody(
            TransferEntity transferEntity,
            Optional<SavedRecipientEntity> savedRecipient,
            InitialTransferResponse initialResponse) {

        MultivaluedMapImpl transferRequestBody = new MultivaluedMapImpl();

        if (savedRecipient.isPresent()) {
            transferRequestBody.add("nyOverforing.bankkonto.kontonummer", "");
            transferRequestBody.add(
                    "nyOverforing.overforingBefintligMottagare",
                    getRecipientIdentifier(savedRecipient.get()));

        } else {
            String recipient = transferEntity.getDestinationAccountNumber();
            transferRequestBody.add("nyOverforing.bankkonto.kontonummer", recipient);
            Optional<String> newRecipientIdentifier =
                    SavedRecipientEntity.getNewRecipientIdentifier(recipient);

            if (!newRecipientIdentifier.isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage("Could not create a new recipient identifier")
                        .build();
            }

            transferRequestBody.add(
                    "nyOverforing.overforingBefintligMottagare", newRecipientIdentifier.get());
        }

        transferRequestBody.add("nyOverforing.formattedBelopp", transferEntity.getPositiveAmount());
        transferRequestBody.add("nyOverforing.datumBetalning", transferEntity.getDate());
        transferRequestBody.add("nyOverforing.mottagarNamn", "");
        transferRequestBody.add("nyOverforing.sparaOverforingsmottagare", "false");
        transferRequestBody.add(
                "nyOverforing.franKontonummer", transferEntity.getFromAccountNumber());
        transferRequestBody.add(
                "nyOverforing.meddelandeTillMottagare", transferEntity.getDestinationMessage());
        transferRequestBody.add("nyOverforing.egenNotering", transferEntity.getSourceMessage());

        // Note: the 'true' checkbox value here is always sent as true from the web page, but the
        // real value
        // (nyOverforing.staendeOverforing) is only sent and set to 'true' when it is a recurring
        // transfer.

        transferRequestBody.add("__checkbox_nyOverforing.staendeOverforing", "true");
        transferRequestBody.add("struts.token.name", initialResponse.getStrutsTokenName());
        transferRequestBody.add("token", initialResponse.getToken());

        return transferRequestBody;
    }

    private String getRecipientIdentifier(SavedRecipientEntity recipient) {
        Optional<String> identifier = recipient.getSBABIdentifier();

        if (identifier.isPresent()) {
            return identifier.get();
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not create identifier string from saved recipient")
                    .build();
        }
    }

    private String findTransferIdToAccept(TransferEntity actualTransfer, Document resultPage)
            throws Exception {
        Element transfersToAcceptTable = resultPage.select("table.attgodkannaoversikt").first();

        if (transfersToAcceptTable == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not find any transfers to accept.")
                    .build();
        }

        List<TransferEntity> transfersToAccept =
                TransferClientParser.parseTransfersToAccept(transfersToAcceptTable);

        for (TransferEntity transferToAcceptEntity : transfersToAccept) {
            if (transferToAcceptEntity.equals(actualTransfer)
                    && transferToAcceptEntity.getId() != null) {
                return transferToAcceptEntity.getId();
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Could not find a transfer to accept matching the given transfer to be executed.")
                .build();
    }
}
