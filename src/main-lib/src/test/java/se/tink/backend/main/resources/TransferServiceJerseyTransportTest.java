package se.tink.backend.main.resources;

import com.google.common.collect.Lists;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.WebApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.GiroRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Amount;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.controllers.TransferServiceController;
import se.tink.backend.main.providers.transfer.TransferSourceAccountProvider;
import se.tink.backend.main.providers.transfer.UserTransferDestinationProvider;
import se.tink.backend.main.rpc.TransferEnricher;
import se.tink.backend.main.transports.TransferServiceJerseyTransport;
import se.tink.backend.main.validators.TransferRequestValidator;
import se.tink.backend.main.validators.TransferUpdateRequestValidator;
import se.tink.backend.main.validators.TransferValidator;
import se.tink.backend.main.validators.exception.TransferEnricherException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.backend.main.validators.exception.TransfersTemporaryDisabledException;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;

public class TransferServiceJerseyTransportTest {
    private static final String USER_ID = "627fba23f10a4eb9bb667be6f144151f";
    private static final String CREDENTIALS_ID = "627fba23f10a4eb9bb667be6f144152f";

    private Credentials credentials;

    private TransferRequestValidator transferRequestValidator;
    private TransferUpdateRequestValidator transferUpdateRequestValidator;
    private TransferEnricher transferEnricher;

    private TransferServiceJerseyTransport transferServiceJerseyTransport;
    private TransferServiceController transferServiceController;
    private TransferRepository transferRepository;

    private static final AccountIdentifier SOURCE_IDENTIFIER = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "199001010000");
    private static final AccountIdentifier BARNCANCERFONDEN_BG = AccountIdentifier
            .create(AccountIdentifier.Type.SE_BG, "9020900");

    private AuthenticatedUser authenticatedUser;

    private Catalog catalog;

    @Before
    public void setup()
            throws TransfersTemporaryDisabledException, TransferValidationException, TransferEnricherException {
        authenticatedUser = createAuthenticatedUser(FeatureFlags.TRANSFERS);
        credentials = createCredentials();
        catalog = Catalog.getCatalog(authenticatedUser.getLocale());

        this.transferRequestValidator = mock(TransferRequestValidator.class);
        this.transferUpdateRequestValidator = mock(TransferUpdateRequestValidator.class);
        this.transferEnricher = mockTransferEnricher();
        this.transferRepository = mockTransferRepository();

        this.transferServiceController = Mockito.spy(new TransferServiceController(
                mock(GiroRepository.class),
                mock(ProviderDao.class),
                mock(SignableOperationRepository.class),
                mock(TransactionDao.class),
                mock(TransferEventRepository.class),
                transferRepository,
                mockedRefreshCredentialsFactory(),
                mock(AnalyticsController.class),
                mock(TransferSourceAccountProvider.class),
                mock(UserTransferDestinationProvider.class),
                mock(ProviderImageProvider.class),
                transferEnricher,
                transferRequestValidator,
                mock(TransferValidator.class),
                transferUpdateRequestValidator,
                mock(ListenableThreadPoolExecutor.class)));

        transferServiceJerseyTransport = new TransferServiceJerseyTransport(transferServiceController);
    }

    @Test
    public void ensureCreate_callMethodsInOrder_firstValidateRequest_thenMakeReliableCopy()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer reliableTransfer = createTransfer();

        Transfer transfer = mock(Transfer.class);
        doNothing().when(transferRequestValidator).validate(any(Transfer.class));
        when(transfer.makeReliableCopy()).thenReturn(reliableTransfer);

        transferServiceJerseyTransport.createTransfer(authenticatedUser, transfer);

        InOrder order = inOrder(transferRequestValidator, transfer);
        order.verify(transferRequestValidator).validate(any(Transfer.class));
        order.verify(transfer).makeReliableCopy();
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferTemporaryDisabledException_isThrownFromCreate()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer();

        TransfersTemporaryDisabledException exception = TransfersTemporaryDisabledException.builder()
                .setEndUserMessage(EndUserMessage.TEMPORARY_DISABLED)
                .build();

        Mockito.doThrow(exception).when(transferRequestValidator).validate(transfer);

        SignableOperation signableOperation = transferServiceJerseyTransport
                .createTransfer(authenticatedUser, transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(UUIDUtils.fromTinkUUID(USER_ID), signableOperation.getUserId());
        assertEquals(EndUserMessage.TEMPORARY_DISABLED.getKey().get(), signableOperation.getStatusMessage());
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferTemporaryDisabledException_isThrownFromUpdate()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer();

        TransfersTemporaryDisabledException exception = TransfersTemporaryDisabledException.builder()
                .setEndUserMessage(EndUserMessage.TEMPORARY_DISABLED)
                .build();

        Mockito.doThrow(exception).when(transferUpdateRequestValidator).validate(any(Transfer.class));

        SignableOperation signableOperation = transferServiceJerseyTransport.update(authenticatedUser,
                UUIDUtils.toTinkUUID(transfer.getId()), transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(UUIDUtils.fromTinkUUID(USER_ID), signableOperation.getUserId());
        assertEquals(EndUserMessage.TEMPORARY_DISABLED.getKey().get(), signableOperation.getStatusMessage());
    }

    @Test
    public void ensureHttpStatusForbidden_isReturned_whenTransferTemporaryDisabledException_isThrown()
            throws TransfersTemporaryDisabledException {
        Transfer transfer = createTransfer();

        TransfersTemporaryDisabledException exception = TransfersTemporaryDisabledException.builder()
                .setEndUserMessage(EndUserMessage.TEMPORARY_DISABLED)
                .build();

        Mockito.doThrow(exception).when(transferRequestValidator).validateEnabled();

        try {
            transferServiceJerseyTransport.get(authenticatedUser, UUIDUtils.toTinkUUID(transfer.getId()));

            throw new AssertionError("Expected exception to be thrown", exception);
        } catch (WebApplicationException e) {
            assertEquals(503, e.getResponse().getStatus());
        }
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferValidationException_isThrownFromCreate()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer();

        TransferValidationException exception = TransferValidationException.builder(transfer)
                .setLogMessage(LogMessage.MISSING_AMOUNT)
                .setEndUserMessage(EndUserMessage.MISSING_AMOUNT)
                .build();

        Mockito.doThrow(exception).when(transferRequestValidator).validate(transfer);

        SignableOperation signableOperation = transferServiceJerseyTransport
                .createTransfer(authenticatedUser, transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(EndUserMessage.MISSING_AMOUNT.getKey().get(), signableOperation.getStatusMessage());
        assertEquals(transfer.getUserId(), signableOperation.getUserId());
        assertEquals(transfer.getId(), signableOperation.getUnderlyingId());
    }

    @Test
    public void ensureTransfer_hasOriginalTransferField_whenUpdateTransfer() {
        Transfer transfer = createTransfer();

        SignableOperation signableOperation = transferServiceJerseyTransport.update(authenticatedUser,
                UUIDUtils.toTinkUUID(transfer.getId()), transfer);

        Transfer signableOperationTransfer = SerializationUtils
                .deserializeFromString(signableOperation.getSignableObject(), Transfer.class);

        Assert.assertNotNull(signableOperationTransfer);
        Assert.assertTrue(signableOperationTransfer.getOriginalTransfer().isPresent());
    }

    @Test
    public void ensureTransfer_changeOriginalIdentifiers_whenUpdateTransfer()
            throws TransferValidationException , TransferEnricherException {
        Transfer transfer = createTransfer();
        transfer.setSource(AccountIdentifier.create(URI.create("se://6000111111111")));
        transfer.setOriginalSource(AccountIdentifier.create(URI.create("se://60002222222222")));
        transfer.setDestination(AccountIdentifier.create(URI.create("se://60003333333333")));
        transfer.setOriginalDestination(AccountIdentifier.create(URI.create("se://60004444444444")));

        transferServiceJerseyTransport.update(authenticatedUser, UUIDUtils.toTinkUUID(transfer.getId()), transfer);

        ArgumentCaptor<Transfer> updatedTransferCaptor = ArgumentCaptor.forClass(Transfer.class);
        Mockito.verify(transferServiceController)
                .makeTransfer(any(User.class), updatedTransferCaptor.capture(), eq(true),
                        any(Optional.class));

        Transfer updatedTransfer = updatedTransferCaptor.getValue();

        Assert.assertNotNull(updatedTransfer);
        Assert.assertEquals(updatedTransfer.getSource(), updatedTransfer.getOriginalSource());
        Assert.assertEquals(updatedTransfer.getDestination(),
                updatedTransfer.getOriginalDestination());
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferValidationException_isThrownFromUpdate()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer();

        TransferValidationException exception = TransferValidationException.builder(transfer)
                .setLogMessage(LogMessage.MISSING_AMOUNT)
                .setEndUserMessage(EndUserMessage.MISSING_AMOUNT)
                .build();

        Mockito.doThrow(exception).when(transferUpdateRequestValidator).validate(any(Transfer.class));

        SignableOperation signableOperation = transferServiceJerseyTransport.update(authenticatedUser,
                UUIDUtils.toTinkUUID(transfer.getId()), transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(EndUserMessage.MISSING_AMOUNT.getKey().get(), signableOperation.getStatusMessage());
        assertEquals(transfer.getUserId(), signableOperation.getUserId());
        assertEquals(transfer.getId(), signableOperation.getUnderlyingId());
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferEnricherException_isThrownFromCreate()
            throws TransferValidationException, TransferEnricherException {
        Transfer transfer = createTransfer();

        TransferEnricherException exception = TransferEnricherException.builder(transfer)
                .setLogMessage(LogMessage.NO_MATCH_ACCOUNTS_PATTERNS)
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .build();

        Mockito.doThrow(exception).when(transferEnricher).enrichAndGetCredentials(any(Transfer.class), eq(catalog));

        SignableOperation signableOperation = transferServiceJerseyTransport
                .createTransfer(authenticatedUser, transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(EndUserMessage.INVALID_DESTINATION.getKey().get(), signableOperation.getStatusMessage());
        assertEquals(transfer.getUserId(), signableOperation.getUserId());
        assertEquals(transfer.getId(), signableOperation.getUnderlyingId());
    }

    @Test
    public void ensureSignableOperation_isReturned_whenTransferEnricherException_isThrownFromUpdate()
            throws TransferValidationException, TransferEnricherException {
        Transfer transfer = createTransfer();

        TransferEnricherException exception = TransferEnricherException.builder(transfer)
                .setLogMessage(LogMessage.NO_MATCH_ACCOUNTS_PATTERNS)
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .build();

        Mockito.doThrow(exception).when(transferEnricher).enrichAndGetCredentials(any(Transfer.class), eq(catalog));

        SignableOperation signableOperation = transferServiceJerseyTransport.update(authenticatedUser,
                UUIDUtils.toTinkUUID(transfer.getId()), transfer);

        assertEquals(SignableOperationStatuses.FAILED, signableOperation.getStatus());
        assertEquals(EndUserMessage.INVALID_DESTINATION.getKey().get(), signableOperation.getStatusMessage());
        assertEquals(transfer.getUserId(), signableOperation.getUserId());
        assertEquals(transfer.getId(), signableOperation.getUnderlyingId());
    }

    @Test
    public void ensureBadRequestIsThrown_whenTransferId_isInvalidFormat()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer();

        try {
            transferServiceJerseyTransport.update(authenticatedUser, "12345", transfer);

        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void ensureNotFoundIsThrown_whenTransfer_couldNotBeFoundInDatabase() {
        try {
            Transfer transfer = createTransfer();

            when(transferRepository.findOneByUserIdAndId(any(String.class), any(String.class)))
                    .thenReturn(null);

            transferServiceJerseyTransport.update(authenticatedUser, UUIDUtils.toTinkUUID(transfer.getId()), transfer);
        } catch (WebApplicationException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }

    @Test
    public void ensureForbiddenIsThrown_whenTransferFoundInDatabase_doesNotBelongToTheLoggedInUser() {
        try {
            Transfer transfer = createTransfer();

            authenticatedUser.getUser().setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));

            transferServiceJerseyTransport.update(authenticatedUser, UUIDUtils.toTinkUUID(transfer.getId()), transfer);

            throw new AssertionError("Expected exception to be thrown", new WebApplicationException());
        } catch (WebApplicationException e) {
            assertEquals(403, e.getResponse().getStatus());
        }
    }

    private void insertExistingTransfer(Transfer existingTransfer) {
        when(transferRepository.findOneByUserIdAndId(any(String.class), any(String.class)))
                .thenReturn(existingTransfer);
    }

    private Transfer createTransfer() {
        return createTransfer("default message");
    }

    private Transfer createTransfer(String message) {
        return createTransfer(BARNCANCERFONDEN_BG, message);
    }

    private Transfer createTransfer(AccountIdentifier destination, String message) {
        return createTransfer(SOURCE_IDENTIFIER, destination, message);
    }

    private Transfer createTransfer(AccountIdentifier source, AccountIdentifier destination, String message) {
        Transfer transfer = new Transfer();

        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(DateUtils.getCurrentOrNextBusinessDay());
        transfer.setOriginalSource(source);
        transfer.setSource(source);
        transfer.setOriginalDestination(destination);
        transfer.setDestination(destination);
        transfer.setAmount(Amount.inSEK(20.00));
        transfer.setUserId(UUIDUtils.fromTinkUUID(USER_ID));
        transfer.setSourceMessage(String.format("Source message: %s", message));
        transfer.setDestinationMessage(String.format("Destination message: %s", message));

        return transfer;
    }

    private AuthenticatedUser createAuthenticatedUser(String... flags) {
        User user = createUser(flags);
        return new AuthenticatedUser(HttpAuthenticationMethod.SESSION, user);
    }

    private User createUser(String... flags) {
        User user = new User();
        user.setId(USER_ID);
        UserProfile profile = new UserProfile();
        profile.setLocale("en_US");
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList(flags));

        return user;
    }

    private Credentials createCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(CREDENTIALS_ID);
        credentials.setUserId(USER_ID);

        return credentials;
    }

    private TransferDestinationPattern createPatternFor(AccountIdentifier.Type type) {
        TransferDestinationPattern tdp = new TransferDestinationPattern();
        tdp.setType(type);
        tdp.setPattern(".+");

        return tdp;
    }

    private List<TransferDestinationPattern> createPatterns(AccountIdentifier.Type... types) {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();

        for (AccountIdentifier.Type type : types) {
            patterns.add(createPatternFor(type));
        }

        return patterns;
    }

    private TransferEnricher mockTransferEnricher()
            throws TransferValidationException, TransferEnricherException {
        TransferEnricher transferEnricher = mock(TransferEnricher.class);

        when(transferEnricher.enrichAndGetCredentials(any(Transfer.class), eq(catalog)))
                .thenReturn(credentials);

        return transferEnricher;
    }

    private TransferRepository mockTransferRepository()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        TransferRepository transferRepository = mock(TransferRepository.class);

        when(transferRepository.findOneByUserIdAndId(any(String.class), any(String.class)))
                .thenReturn(createTransfer());

        return transferRepository;
    }

    private CredentialsRequestRunnableFactory mockedRefreshCredentialsFactory()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        CredentialsRequestRunnableFactory refreshCredentialsFactory = mock(CredentialsRequestRunnableFactory.class);
        Runnable runnable = mock(Runnable.class);

        when(refreshCredentialsFactory.createTransferRunnable(
                any(User.class), any(Credentials.class), any(SignableOperation.class), any(boolean.class)))
                .thenReturn(runnable);

        return refreshCredentialsFactory;
    }

    private CredentialsRepository mockedCredentialsRepository()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        CredentialsRepository credentialsRepository = mock(CredentialsRepository.class);
        Credentials credentials = mock(Credentials.class);

        when(credentials.getId())
                .thenReturn(StringUtils.generateUUID());
        when(credentialsRepository.findOne(any(String.class)))
                .thenReturn(credentials);

        return credentialsRepository;
    }

    private TransferDestinationPatternRepository mockedTransferDestinationPatternRepository()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        TransferDestinationPatternRepository transferDestinationPatternRepository = mock(
                TransferDestinationPatternRepository.class);

        List<TransferDestinationPattern> validPatterns = createPatterns(AccountIdentifier.Type.SE_PG,
                AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE);

        when(transferDestinationPatternRepository.findAllByUserIdAndAccountId(any(String.class), any(String.class)))
                .thenReturn(validPatterns);

        return transferDestinationPatternRepository;
    }

    private CurrencyRepository mockedCurrencyRepository()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        CurrencyRepository currencyRepository = mock(CurrencyRepository.class);

        List<Currency> currencies = Lists.newArrayList();
        Currency currency = new Currency();
        currency.setCode("SEK");
        currencies.add(currency);

        when(currencyRepository.findAll()).thenReturn(currencies);

        return currencyRepository;
    }

    private ServiceConfiguration mockedServiceConfiguration()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        TransfersConfiguration transfersConfiguration = mockedTransfersConfiguration();

        ServiceConfiguration serviceConfiguration = mock(ServiceConfiguration.class);
        when(serviceConfiguration.getTransfers())
                .thenReturn(transfersConfiguration);

        return serviceConfiguration;
    }

    private TransfersConfiguration mockedTransfersConfiguration()
            throws TransfersTemporaryDisabledException, TransferValidationException {
        TransfersConfiguration transfersConfiguration = mock(TransfersConfiguration.class);
        when(transfersConfiguration.isEnabled()).thenReturn(true);
        when(transfersConfiguration.getPaymentThreshold()).thenReturn(200000);

        return transfersConfiguration;
    }
}
