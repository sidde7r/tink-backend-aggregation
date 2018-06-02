package se.tink.backend.main.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.GiroRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.utils.giro.lookup.BankGiroCrawler;
import se.tink.backend.common.utils.giro.lookup.LookupGiro;
import se.tink.backend.common.utils.giro.lookup.LookupGiroException;
import se.tink.backend.common.utils.giro.lookup.PlusGiroCrawler;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.main.providers.transfer.TransferSourceAccountProvider;
import se.tink.backend.main.providers.transfer.UserTransferDestinationProvider;
import se.tink.backend.main.rpc.TransferEnricher;
import se.tink.backend.main.validators.TransferRequestValidator;
import se.tink.backend.main.validators.TransferUpdateRequestValidator;
import se.tink.backend.main.validators.TransferValidator;
import se.tink.backend.main.validators.exception.AbstractTransferException;
import se.tink.backend.main.validators.exception.TransferEnricherException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.backend.main.validators.exception.TransfersTemporaryDisabledException;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.ClearingLookupResponse;
import se.tink.backend.rpc.GiroLookupEntity;
import se.tink.backend.rpc.UpdateTransferRequest;
import se.tink.backend.rpc.transfer.GetTransferDestinationsPerAccountCommand;
import se.tink.backend.rpc.transfer.TransferDestinationsPerAccountResult;
import se.tink.backend.utils.ClearingNumberBankToProviderMap;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.ProviderDisplayNameFinder;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifierPredicate;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferServiceController {
    private static final LogUtils log = new LogUtils(TransactionServiceController.class);
    private static final ClearingNumberBankToProviderMap CLEARING_NUMBER_BANK_TO_PROVIDER =
            new ClearingNumberBankToProviderMapImpl();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int LAST_UPDATED_SIGNABLE_OPERATION_TIME_MINUTES = 5;

    private final GiroRepository giroRepository;
    private final ProviderDao providerDao;
    private final SignableOperationRepository signableOperationRepository;
    private final TransactionDao transactionDao;
    private final TransferEventRepository transferEventRepository;
    private final TransferRepository transferRepository;

    private final CredentialsRequestRunnableFactory refreshCredentialsFactory;
    private final AnalyticsController analyticsController;
    private final TransferSourceAccountProvider transferSourceAccountProvider;
    private final UserTransferDestinationProvider userTransferDestinationProvider;
    private final ProviderImageProvider providerImageProvider;

    private final TransferEnricher transferEnricher;
    private final TransferRequestValidator transferRequestValidator;
    private final TransferValidator transferValidator;
    private final TransferUpdateRequestValidator transferUpdateRequestValidator;
    private final ListenableThreadPoolExecutor<Runnable> executor;

    private final LookupGiro giroFinder;


    @Inject
    public TransferServiceController(
            GiroRepository giroRepository,
            ProviderDao providerDao, SignableOperationRepository signableOperationRepository,
            TransactionDao transactionDao,
            TransferEventRepository transferEventRepository,
            TransferRepository transferRepository,
            CredentialsRequestRunnableFactory refreshCredentialsFactory,
            AnalyticsController analyticsController,
            TransferSourceAccountProvider transferSourceAccountProvider,
            UserTransferDestinationProvider userTransferDestinationProvider,
            ProviderImageProvider providerImageProvider,
            TransferEnricher transferEnricher,
            TransferRequestValidator transferRequestValidator,
            TransferValidator transferValidator,
            TransferUpdateRequestValidator transferUpdateRequestValidator,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor) {
        this.giroRepository = giroRepository;
        this.providerDao = providerDao;
        this.signableOperationRepository = signableOperationRepository;
        this.transactionDao = transactionDao;
        this.transferEventRepository = transferEventRepository;
        this.transferRepository = transferRepository;
        this.refreshCredentialsFactory = refreshCredentialsFactory;
        this.analyticsController = analyticsController;
        this.transferSourceAccountProvider = transferSourceAccountProvider;
        this.userTransferDestinationProvider = userTransferDestinationProvider;
        this.providerImageProvider = providerImageProvider;
        this.transferEnricher = transferEnricher;
        this.transferRequestValidator = transferRequestValidator;
        this.transferValidator = transferValidator;
        this.transferUpdateRequestValidator = transferUpdateRequestValidator;
        this.executor = executor;

        this.giroFinder = new LookupGiro(giroRepository, new BankGiroCrawler(), new PlusGiroCrawler());
    }

    public SignableOperation create(User user, Transfer incomingTransfer, Optional<String> remoteAddress)
            throws TransferNotFoundException {
        try {
            transferRequestValidator.validate(incomingTransfer);

            Transfer reliableTransfer = incomingTransfer.makeReliableCopy();
            reliableTransfer.setUserId(UUIDUtils.fromTinkUUID(user.getId()));

            log.info(reliableTransfer, "Calling makeTransfer from create");
            return makeTransfer(user, reliableTransfer, false, remoteAddress);
        } catch (TransferValidationException | TransferEnricherException | TransfersTemporaryDisabledException e) {
            e.logDetails(user, log);

            // Since the app don't handle Status.CANCELLED we always need to return FAILED (temporarily)
            SignableOperation signableOperation = e.getSignableOperation(user);
            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            // Clean any sensitive data from the SignableOperation before returning it
            signableOperation.cleanSensitiveData();

            return signableOperation;
        }
    }

    public TransferDestination createDestination(User user, URI uri, String name)
            throws DuplicateException, UnsupportedOperationException {
        validateEnabled(user);

        return userTransferDestinationProvider.createDestination(user, uri, name);
    }

    public SignableOperation update(User user, String id, UpdateTransferRequest updateTransferRequest,
            Optional<String> remoteAddress)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException,
            UnsupportedOperationException, TransferNotFoundException {
        Transfer existingTransfer = getTransferInternal(user, id);
        Transfer updatedTransfer = modifyUpdatedProperties(existingTransfer, updateTransferRequest);

        return update(user, id, updatedTransfer, remoteAddress);
    }

    public SignableOperation update(User user, String id, Transfer incomingTransfer,
            Optional<String> remoteAddress)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException,
            UnsupportedOperationException, TransferNotFoundException {

        try {
            transferUpdateRequestValidator.validateEnabled();
            Transfer existingTransfer = getTransferInternal(user, id);
            Transfer updatedTransfer = incomingTransfer.makeReliableCopy(existingTransfer);
            // Verify transfer after updating it, so we don't rely on unmodified incoming value
            verifyTransfer(updatedTransfer, existingTransfer);

            log.info(updatedTransfer, "Calling makeTransfer from update");
            return makeTransfer(user, updatedTransfer, true, remoteAddress);
        } catch (TransferValidationException | TransferEnricherException | TransfersTemporaryDisabledException e) {
            e.logDetails(user, log);

            // Since the app don't handle Status.CANCELLED we always need to return FAILED (temporarily)
            SignableOperation signableOperation = e.getSignableOperation(user);
            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            // Clean any sensitive data from the SignableOperation before returning it
            signableOperation.cleanSensitiveData();

            return signableOperation;
        }
    }

    public Transfer get(User user, String id) throws IllegalArgumentException, IllegalStateException,
            NoSuchElementException, UnsupportedOperationException {
        Transfer transfer = getTransferInternal(user, id);

        transfer.clearInternalInformation();
        return transfer;
    }

    public List<Transfer> list(User user, final TransferType type)
            throws IllegalStateException, UnsupportedOperationException {
        validateEnabled(user);

        List<Transfer> transfers = transferRepository.findAllByUserId(user.getId());

        if (type != null) {
            transfers = transfers.stream().filter(input -> Objects.equal(input.getType(), type))
                    .collect(Collectors.toList());
        }

        for (Transfer transfer : transfers) {
            if (!user.getId().equals(UUIDUtils.toTinkUUID(transfer.getUserId()))) {
                throw new IllegalStateException("Transfer doesn't belong to logged in user");
            }

            transfer.clearInternalInformation();
        }

        return transfers;
    }

    public SignableOperation getSignableOperation(User user, String id) throws IllegalArgumentException,
            NoSuchElementException, UnsupportedOperationException {
        validateEnabled(user);

        if (!UUIDUtils.isValidTinkUUID(id)) {
            throw new IllegalArgumentException("Misformed Tink UUID");
        }

        String userId = user.getId();

        Optional<SignableOperation> operation = signableOperationRepository.findOneByUserIdAndUnderlyingId(
                userId, UUIDUtils.fromTinkUUID(id));

        if (!operation.isPresent()) {
            throw new NoSuchElementException("Signable operation was not found");
        }

        // Clean any sensitive data from the SignableOperation before returning it
        operation.get().cleanSensitiveData();

        return operation.get();
    }

    public List<SignableOperation> listSignableOperations(User user) {
        if (!FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE.isFlagInGroup(user.getFlags())) {
            return Collections.emptyList();
        }
        return signableOperationRepository.findAllByUserId(user.getId())
                .stream()
                .map(signableOperation -> {
                    signableOperation.cleanSensitiveData();
                    return signableOperation;
                })
                .collect(Collectors.toList());
    }

    public List<SignableOperation> getMostRecentSignableOperations(User user) {
        return getMostRecentSignableOperations(user, LAST_UPDATED_SIGNABLE_OPERATION_TIME_MINUTES);
    }

    public List<SignableOperation> getMostRecentSignableOperations(User user, int minutes) {
        Date startDate = DateUtils.addMinutes(new Date(), -minutes);

        return listSignableOperations(user)
                .stream()
                .filter(signableOperation -> signableOperation.getUpdated().after(startDate))
                .collect(Collectors.toList());
    }

    public List<GiroLookupEntity> giroLookup(User user, String giro)
            throws IllegalArgumentException, NoSuchElementException, UnsupportedOperationException {
        validateEnabled(user);

        if (Strings.isNullOrEmpty(giro)) {
            throw new IllegalArgumentException("Not a valid giro number: " + giro);
        }

        try {
            List<AccountIdentifier> identifiers = giroFinder.lookup(giro);
            List<GiroLookupEntity> giroEntities = Lists.newArrayList();

            for (AccountIdentifier identifier : identifiers) {
                GiroLookupEntity giroEntity = new GiroLookupEntity();

                giroEntity.setDisplayName(identifier.getName().orElse(null));
                giroEntity.setDisplayNumber(identifier.getIdentifier(new DisplayAccountIdentifierFormatter()));
                giroEntity.setIdentifier(identifier.toURI());
                giroEntity.setImages(providerImageProvider.get().getImagesForAccountIdentifier(identifier));

                giroEntities.add(giroEntity);
            }

            return giroEntities;

        } catch (LookupGiroException e) {
            switch (e.getType()) {
            case NOT_FOUND:
                throw new NoSuchElementException("Couldn't find giro account: " + giro);
            case INVALID_FORMAT:
                throw new IllegalArgumentException("Not a valid giro number: " + giro);
            }
        }
        return null;
    }

    public AccountListResponse getSourceAccounts(User user, Set<AccountIdentifier.Type> explicitTypeFilter,
            Set<URI> explicitIdentifierFilter) throws UnsupportedOperationException {
        validateEnabled(user);

        Optional<Set<AccountIdentifier.Type>> explicitTypes = Optional.empty();
        if (explicitTypeFilter != null && !explicitTypeFilter.isEmpty()) {
            explicitTypes = Optional.of(explicitTypeFilter);
        }

        Optional<Set<AccountIdentifier>> explicitIdentifiers = Optional.empty();
        if (explicitIdentifierFilter != null && !explicitIdentifierFilter.isEmpty()) {

            Set<AccountIdentifier> set = explicitIdentifierFilter.stream()
                    .map(AccountIdentifier::create)
                    .filter(AccountIdentifierPredicate.IS_VALID::apply)
                    .collect(Collectors.toSet());

            explicitIdentifiers = Optional.of(set);
        }

        List<Account> sourceAccounts = transferSourceAccountProvider
                .getSourceAccounts(user, explicitTypes, explicitIdentifiers);

        return new AccountListResponse(sourceAccounts);
    }

    public AccountListResponse getSourceAccountsForTransfer(User user, String id)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException,
            UnsupportedOperationException {
        // Use validation of TransferService#getTransferInternal
        Transfer transfer = getTransferInternal(user, id);

        Set<AccountIdentifier.Type> typeFilter = ImmutableSet.of();
        Set<URI> destinationFilter = ImmutableSet.of(transfer.getDestination().toURI());

        AccountListResponse sourceAccountsResponse = getSourceAccounts(user, typeFilter, destinationFilter);

        List<Account> sourceAccountsForEInvoice = filterAccountsForTransfer(sourceAccountsResponse, transfer);
        sourceAccountsResponse.setAccounts(sourceAccountsForEInvoice);

        // Note: this is a temporary solution to change the name of the destinations to the source message of the
        // transfers, which could not (at the time of this commit) be handled on frontend.
        for (Account account : sourceAccountsResponse.getAccounts()) {
            for (TransferDestination destination : account.getTransferDestinations()) {
                if (!Strings.isNullOrEmpty(transfer.getSourceMessage())) {
                    destination.setName(StringUtils.formatHuman(transfer.getSourceMessage()));
                }
            }
        }

        return sourceAccountsResponse;
    }

    public ClearingLookupResponse clearingLookup(String clearing) {
        if (!ClearingNumber.isValidClearing(clearing)) {
            throw new IllegalArgumentException("Not a valid clearing number");
        }

        Optional<ClearingNumber.Details> details = ClearingNumber.get(clearing);

        if (!details.isPresent()) {
            throw new NoSuchElementException("Not a valid clearing number");
        }

        Optional<String> providerName = getProviderName(details.get());

        ProviderImage providerImageIcon = providerImageProvider.get()
                .find(ProviderImage.Type.ICON, providerName.orElse(null));
        ProviderImage providerImageBanner = providerImageProvider.get()
                .find(ProviderImage.Type.BANNER, providerName.orElse(null));

        ImageUrls images = new ImageUrls();

        if (providerImageIcon != null) {
            images.setIcon(providerImageIcon.getUrl());
        }
        if (providerImageBanner != null) {
            images.setBanner(providerImageBanner.getUrl());
        }

        ClearingLookupResponse response = new ClearingLookupResponse();
        response.setImages(images);
        response.setBankDisplayName(details.get().getBankName());
        return response;
    }

    /**
     * For given non-null details first try to find provider from static map, if not found try to guess it based on bank
     * name from a display name finder
     */
    private Optional<String> getProviderName(ClearingNumber.Details details) {
        Optional<String> providerName = CLEARING_NUMBER_BANK_TO_PROVIDER.getProviderForBank(details.getBank());
        if (providerName.isPresent()) {
            return providerName;
        }

        ProviderDisplayNameFinder displayNameFinder =
                new ProviderDisplayNameFinder(providerDao.getProvidersByName(), Lists.<Credentials>newArrayList());

        return Optional.ofNullable(
                displayNameFinder.tryTurnDisplayNameIntoProviderName(details.getBankName())
        );
    }

    private Transfer getTransferInternal(User user, String id) throws IllegalArgumentException, IllegalStateException,
            NoSuchElementException, UnsupportedOperationException {
        validateEnabled(user);

        if (!UUIDUtils.isValidTinkUUID(id)) {
            throw new IllegalArgumentException("Misformed Tink UUID");
        }

        Transfer transfer = transferRepository.findOneByUserIdAndId(user.getId(), id);

        if (transfer == null) {
            // This check to find a transaction with the given transfer id on it can be removed when apps read
            // transaction payload EDITABLE_TRANSACTION_TRANSFER instead of EDITABLE_TRANSACTION_TRANSFER_ID
            // When removing this, also remove in agents that Transfer explicitly gets the same id as the transaction.
            try {
                Transaction transaction = transactionDao.findOneByUserIdAndId(user.getId(), id, Optional.empty());
                if (transaction != null) {
                    String value = transaction.getPayloadValue(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER);

                    if (value != null) {
                        Transfer possibleTransfer = MAPPER.readValue(value, Transfer.class);

                        if (id.equals(UUIDUtils.toTinkUUID(possibleTransfer.getId()))) {
                            if (user.getId().equals(transaction.getUserId())) {
                                possibleTransfer.setUserId(
                                        UUIDUtils.fromTinkUUID(transaction.getUserId()));
                                possibleTransfer.setCredentialsId(
                                        UUIDUtils.fromTinkUUID(transaction.getCredentialsId()));
                                transfer = possibleTransfer;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception when trying to read transfer from transaction payload", e);
            }
        }

        if (transfer == null) {
            throw new NoSuchElementException("Transfer doesn't exist");
        }

        if (!user.getId().equals(UUIDUtils.toTinkUUID(transfer.getUserId()))) {
            throw new IllegalStateException("Transfer doesn't belong to logged in user");
        }

        return transfer;
    }

    /**
     * For existing transfers we cannot change the credential, so filter out any account not belonging to the transfer's
     * credential id
     */
    private List<Account> filterAccountsForTransfer(AccountListResponse sourceAccountsResponse,
            Transfer transfer) {
        List<Account> sourceAccounts = sourceAccountsResponse.getAccounts();

        if (transfer.getCredentialsId() == null) {
            return sourceAccounts;
        }

        UUID transferCredentialsId = transfer.getCredentialsId();

        return sourceAccounts.stream()
                .filter(AccountPredicate.accountBelongsToCredential(transferCredentialsId)::apply)
                .collect(Collectors.toList());
    }

    public SignableOperation makeTransfer(User user, Transfer transfer, boolean isUpdate,
            Optional<String> remoteAddress) throws TransferNotFoundException, TransferValidationException, TransferEnricherException {
        Catalog catalog = Catalog.getCatalog(user.getLocale());
        Credentials credentials = transferEnricher.enrichAndGetCredentials(transfer, catalog);
        transferValidator.validate(transfer);

        SignableOperation signableOperation = SignableOperation.create(transfer, SignableOperationStatuses.CREATED);
        signableOperationRepository.save(signableOperation);

        // Note that system container does not have any knowledge of the client IP that made this code run.
        transferEventRepository.save(new TransferEvent("main:make-transfer:created", transfer, signableOperation,
                remoteAddress));

        Runnable runnable = refreshCredentialsFactory
                .createTransferRunnable(user, credentials, signableOperation, isUpdate);

        if (runnable != null) {
            transferValidator.rememberTransferToExecute(transfer);

            executor.execute(runnable);
        } else {
            // Note that system container does not have any knowledge of the client IP that made this code run.
            transferEventRepository
                    .save(new TransferEvent("main:make-transfer:no-runnable-given", transfer, signableOperation,
                            remoteAddress));

            throw TransferValidationException.builder(transfer)
                    .setLogMessage(AbstractTransferException.LogMessage.INVALID_RUNNABLE)
                    .setEndUserMessage(AbstractTransferException.EndUserMessage.VERIFY_BANK_CONNECTION)
                    .build(SignableOperationStatuses.FAILED);
        }

        // User tracking.
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Type", transfer.getType());
        analyticsController.trackUserEvent(user, "transfer.initiated", properties, remoteAddress);

        return signableOperation;
    }

    private void validateEnabled(User user) throws UnsupportedOperationException {
        try {
            transferRequestValidator.validateEnabled();
        } catch (TransfersTemporaryDisabledException e) {
            e.logDetails(user, log);
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    private void verifyTransfer(Transfer incomingTransfer, Transfer existingTransfer) throws TransferNotFoundException, TransferValidationException, TransfersTemporaryDisabledException {
        transferUpdateRequestValidator.validate(incomingTransfer);
        transferUpdateRequestValidator.validateUpdates(incomingTransfer, existingTransfer);
    }

    private Transfer modifyUpdatedProperties(Transfer transfer, UpdateTransferRequest updateTransferRequest) {
        Transfer updated = transfer.clone();
        Optional.ofNullable(updateTransferRequest.getAmount()).ifPresent(updated::setAmount);
        Optional.ofNullable(updateTransferRequest.getDestination()).ifPresent(updated::setDestination);
        Optional.ofNullable(updateTransferRequest.getDestinationMessage()).ifPresent(updated::setDestinationMessage);
        Optional.ofNullable(updateTransferRequest.getSource()).ifPresent(updated::setSource);
        Optional.ofNullable(updateTransferRequest.getSourceMessage()).ifPresent(updated::setSourceMessage);
        Optional.ofNullable(updateTransferRequest.getDueDate()).ifPresent(updated::setDueDate);

        return updated;
    }

    public List<TransferDestinationsPerAccountResult> getTransferDestinationsPerAccount(GetTransferDestinationsPerAccountCommand command) {
        List<Account> accounts = transferSourceAccountProvider.getSourceAccounts(command.getUser(), Optional.empty(), Optional.empty());
        List<TransferDestinationsPerAccountResult> destinations = Lists.newArrayList();
        accounts.forEach(account -> {
            destinations.add(new TransferDestinationsPerAccountResult(account.getId(), account.getTransferDestinations()));
        });
        return destinations;
    }
}
