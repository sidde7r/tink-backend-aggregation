package se.tink.backend.main.validators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Amount;
import se.tink.backend.core.Currency;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessageParametrized;

public class TransferValidatorTest {
    private static final int TRANSFER_AMOUNT_THRESHOLD = 20000;
    private static final int PAYMENT_AMOUNT_THRESHOLD = 15000;
    private static final int DUPLICATE_TIME = 1;
    private static final int AGGREGATION_TIME = 5;

    private static final AccountIdentifier PG_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "4321-8765");
    private static final AccountIdentifier NORDEA_SSN_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537");
    private static final AccountIdentifier SHB_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "6152135538858");

    private static final String USER_ID = "627fba23f10a4eb9bb667be6f144151f";

    private AccountRepository accountRepository;
    private TransferRepository transferRepository;
    private TransferEventRepository transferEventRepository;
    private SignableOperationRepository signableOperationRepository;
    private TransferValidator validator;
    private TransfersConfiguration transferConfiguration;
    private List<SignableOperation> signableOperations = Lists.newArrayList();

    private User user;
    private Transfer transfer;

    private CacheClient cacheClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        accountRepository = mockAccountRepository();
        transferRepository = mockTransferRepository();
        transferEventRepository = mockTransferEventRepository();
        transferConfiguration = mockTransferConfiguration();
        cacheClient = mockCacheClient();

        user = createUser();
        transfer = createValidTransfer();

        signableOperationRepository = mockSignableOperationRepository();

        validator = new TransferValidator(transferConfiguration, cacheClient, getCurrenciesByCode(),
                transferEventRepository, signableOperationRepository, accountRepository);
    }

    @Test
    public void ensureInstantiatingValidator_throwsException_whenTransfersConfiguration_isNull() {
        expect("No TransfersConfiguration provided", InstantiationException.class);

        new TransferValidator(null, cacheClient, getCurrenciesByCode(), transferEventRepository,
                signableOperationRepository, accountRepository);
    }

    @Test
    public void ensureInstantiatingValidator_throwsException_whenMemcachedClient_isNull() {
        expect("No MemcachedClient provided", InstantiationException.class);

        new TransferValidator(transferConfiguration, null, getCurrenciesByCode(), transferEventRepository,
                    signableOperationRepository, accountRepository);
    }

    @Test
    public void ensureInstantiatingValidator_throwsException_whenCurrenciesByCode_isNull() {
        expect("No currencies provided", InstantiationException.class);

        new TransferValidator(transferConfiguration, cacheClient, null, transferEventRepository,
                signableOperationRepository, accountRepository);
    }

    @Test
    public void ensureInstantiatingValidator_throwsException_whenTransferEventRepository_isNull() {
        expect("No TransferEventRepository provided", InstantiationException.class);

        new TransferValidator(transferConfiguration, cacheClient, getCurrenciesByCode(), null,
                signableOperationRepository, accountRepository);
    }

    @Test
    public void ensureInstantiatingValidator_throwsException_whenSignableOperationRepository_isNull() {
        expect("No SignableOperationRepository provided", InstantiationException.class);

        new TransferValidator(transferConfiguration, cacheClient, getCurrenciesByCode(), transferEventRepository,
                null, accountRepository);
    }

    @Test
    public void ensureValidate_throwsBadRequest_whenTransfer_isNull() throws TransferValidationException {
        expect(TransferNotFoundException.MESSAGE, TransferNotFoundException.class);

        validator.validate(null);
    }

    @Test
    public void ensureExceptionIsThrown_whenDuplicateTransfer_isFoundInMemoryCache()
            throws TransferValidationException {
        storeInCache(transfer);

        expect(LogMessageParametrized.DUPLICATE_TRANSFER, DUPLICATE_TIME);

        validator.validate(transfer);
    }

    @Test
    public void ensureExceptionIsThrown_whenDuplicateTransfer_isFoundInMemoryCache_andMemoryTransferWasExecuted()
            throws TransferValidationException {
        storeInCache(transfer);

        expect(LogMessageParametrized.DUPLICATE_TRANSFER, DUPLICATE_TIME);

        SignableOperation s = SignableOperation.create(transfer, SignableOperationStatuses.EXECUTED);
        signableOperations.add(s);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidationPasses_whenNoDuplicateTransfer_isFoundInMemoryCache()
            throws TransferValidationException {
        Transfer rememberedTransfer = createValidTransfer();
        rememberedTransfer.setAmount(Amount.inSEK(2.00));

        storeInCache(rememberedTransfer);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidationPasses_whenDuplicateTransfer_isFoundInMemoryCache_andMemoryTransferFailed()
            throws TransferValidationException {
        storeInCache(transfer);

        SignableOperation s = SignableOperation.create(transfer, SignableOperationStatuses.FAILED);
        signableOperations.add(s);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidationPasses_whenDuplicateTransfer_isFoundInMemoryCache_andMemoryTransferWasCancelled()
            throws TransferValidationException {
        storeInCache(transfer);

        SignableOperation s = SignableOperation.create(transfer, SignableOperationStatuses.CANCELLED);
        signableOperations.add(s);

        validator.validate(transfer);
    }

     /* ----------------------------------------- BANK_TRANSFERS ----------------------------------------- */

    @Test
    public void ensureValidateAmountTooLarge_throwsException_whenTransferAmount_isLargerThan_maxTransferAmountThreshold()
            throws TransferValidationException {
        transfer.setAmount(Amount.inSEK(TRANSFER_AMOUNT_THRESHOLD + 1000.00));

        expect(LogMessageParametrized.TO_LARGE_AMOUNT_BANK_TRANSFER,
                TRANSFER_AMOUNT_THRESHOLD + " " + transfer.getAmount().getCurrency());

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_throwsException_whenTotalTransferAmount_isLargerThan_maxTransferAmountThreshold()
            throws TransferValidationException {
        int totalAmount = mockRecentTransferEventsToSignableOperations(10, TRANSFER_AMOUNT_THRESHOLD, TransferType.BANK_TRANSFER);

        transfer.setAmount(Amount.inSEK(300.00));

        expect(LogMessageParametrized.TO_LARGE_TOTAL_AMOUNT_TRANSFERRED, totalAmount + transfer.getAmount().getValue(),
                TRANSFER_AMOUNT_THRESHOLD);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_validationPasses_whenTotalPaymentAmount_isLargerThan_maxTransferAmountThreshold()
            throws TransferValidationException {
        mockRecentTransfersToSignableOperations(10, (TRANSFER_AMOUNT_THRESHOLD * 2), TransferType.PAYMENT);

        transfer.setAmount(Amount.inSEK(300.00));

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_validationPasses_whenTotalTransferAmount_isSmallerThan_maxTransferAmountThreshold()
            throws TransferValidationException {
        mockRecentTransfersToSignableOperations(10, ((TRANSFER_AMOUNT_THRESHOLD / 3) * 2), TransferType.PAYMENT);
        mockRecentTransfersToSignableOperations(10, ((TRANSFER_AMOUNT_THRESHOLD / 3) * 2), TransferType.BANK_TRANSFER);

        transfer.setAmount(Amount.inSEK((double) TRANSFER_AMOUNT_THRESHOLD / 4));

        validator.validate(transfer);
    }

     /* ----------------------------------------- PAYMENTS ----------------------------------------- */

    @Test
    public void ensureValidateAmountTooLarge_throwsException_whenPaymentAmount_isLargerThan_maxTransferAmountThreshold()
            throws TransferValidationException {
        Amount amount = Amount.inSEK(PAYMENT_AMOUNT_THRESHOLD + 1000.00);

        transfer.setDestination(PG_IDENTIFIER);
        transfer.setDestinationMessage("Payment destination message");
        transfer.setType(TransferType.PAYMENT);
        transfer.setAmount(amount);

        expect(LogMessageParametrized.TO_LARGE_AMOUNT_PAYMENT,
                PAYMENT_AMOUNT_THRESHOLD + " " + transfer.getAmount().getCurrency());

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_throwsException_whenTotalPaymentAmount_isLargerThan_maxPaymentAmountThreshold()
            throws TransferValidationException {
        int totalAmount = mockRecentTransferEventsToSignableOperations(10, PAYMENT_AMOUNT_THRESHOLD, TransferType.PAYMENT);

        transfer.setDestination(PG_IDENTIFIER);
        transfer.setDestinationMessage("Payment destination message");
        transfer.setType(TransferType.PAYMENT);
        transfer.setAmount(Amount.inSEK(300.00));

        expect(LogMessageParametrized.TO_LARGE_TOTAL_AMOUNT_TRANSFERRED, totalAmount + transfer.getAmount().getValue(),
                PAYMENT_AMOUNT_THRESHOLD);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_validationPasses_whenTotalTransferAmount_isLargerThan_maxPaymentAmountThreshold()
            throws TransferValidationException {
        mockRecentTransfersToSignableOperations(10, (PAYMENT_AMOUNT_THRESHOLD * 2), TransferType.BANK_TRANSFER);

        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(PG_IDENTIFIER);
        transfer.setAmount(Amount.inSEK(300.00));

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmountTooLarge_validationPasses_whenTotalPaymentAmount_isSmallerThan_maxPaymentAmountThreshold()
            throws TransferValidationException {
        mockRecentTransfersToSignableOperations(10, ((PAYMENT_AMOUNT_THRESHOLD / 3) * 2), TransferType.PAYMENT);
        mockRecentTransfersToSignableOperations(10, ((PAYMENT_AMOUNT_THRESHOLD / 3) * 2), TransferType.BANK_TRANSFER);

        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(PG_IDENTIFIER);
        transfer.setAmount(Amount.inSEK((double) PAYMENT_AMOUNT_THRESHOLD / 4));

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmountIsEmpty()
            throws TransferValidationException {
        transfer.setAmount(new Amount(null, 20.00));

        expect(LogMessage.INVALID_AMOUNT_UNEXPECTED);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateSource_throwsException_whenNoAccountsFound()
            throws TransferValidationException {
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Lists.<Account>newArrayList());

        expect(LogMessage.NOT_FOUND_ACCOUNTS);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateSource_throwsException_whenSourceDoesntBelongToUser()
            throws TransferValidationException {
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Lists.newArrayList(new Account()));

        expect(LogMessage.NO_MATCH_ACCOUNTS);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateCurrency_throwsException_whenCurrency_notAvailable()
            throws TransferValidationException {
        transfer.setAmount(Amount.inEUR(20.00));

        expect(LogMessage.CURRENCY_NOT_AVAILABLE);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateCurrency_validationPasses_whenCurrency_isAvailable()
            throws TransferValidationException {
        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationIdentifier_isIncompatibleWithTransferType()
            throws TransferValidationException {
        transfer.setType(TransferType.PAYMENT);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_validationPasses_whenDestinationIdentifier_isCompatibleWithTransferType()
            throws TransferValidationException {
        validator.validate(transfer);
    }

     /* ----------------------------------------- HELPERS ----------------------------------------- */

    private int mockRecentTransfersToSignableOperations(int numberOfTransfers, int totalAmount, TransferType type) {
        Double amountValue = Math.floor((totalAmount / numberOfTransfers) + 0.5d);
        Amount amount = Amount.inSEK(amountValue);

        int total = 0;

        for (int i = 0; i < numberOfTransfers; i++) {
            Transfer transfer = createExecutedTransfer(type);
            transfer.setAmount(amount);

            total += amount.getValue();

            when(transferRepository.findOneByUserIdAndId(USER_ID, UUIDUtils.toTinkUUID(transfer.getId())))
                    .thenReturn(transfer);
        }

        return total;
    }

    private int mockRecentTransferEventsToSignableOperations(int numberOfTransfers, int totalAmount, TransferType type) {
        Double amountValue = Math.floor((totalAmount / numberOfTransfers) + 0.5d);
        Amount amount = Amount.inSEK(amountValue);

        int total = 0;

        for (int i = 0; i < numberOfTransfers; i++) {
            TransferEvent transferEvent = createExecutedTransferEvent(type, amount);

            total += amount.getValue();

            when(transferEventRepository.findAllByUserIdAndTransferId(UUIDUtils.fromString(USER_ID), transferEvent.getTransferId()))
                    .thenReturn(ImmutableList.of(transferEvent));
        }

        return total;
    }

    private AccountRepository mockAccountRepository() {
        AccountRepository accountRepository = mock(AccountRepository.class);

        Account account1 = new Account();
        account1.putIdentifier(SHB_IDENTIFIER);

        when(accountRepository.findByUserId(USER_ID)).thenReturn(Lists.newArrayList(account1));

        return accountRepository;
    }

    private TransferRepository mockTransferRepository() {
        return mock(TransferRepository.class);
    }

    private TransferEventRepository mockTransferEventRepository() {
        return mock(TransferEventRepository.class);
    }

    private TransfersConfiguration mockTransferConfiguration() {
        transferConfiguration = mock(TransfersConfiguration.class);

        when(transferConfiguration.getPaymentThreshold()).thenReturn(PAYMENT_AMOUNT_THRESHOLD);
        when(transferConfiguration.getBankTransferThreshold()).thenReturn(TRANSFER_AMOUNT_THRESHOLD);
        when(transferConfiguration.getAggregationTime()).thenReturn(AGGREGATION_TIME);
        when(transferConfiguration.getDuplicateTime()).thenReturn(DUPLICATE_TIME);
        when(transferConfiguration.isEnabled()).thenReturn(true);

        return transferConfiguration;
    }

    private CacheClient mockCacheClient() {
        return mock(CacheClient.class);
    }

    private User createUser() {
        user = new User();
        user.setId(USER_ID);
        UserProfile profile = new UserProfile();
        profile.setLocale("en_US");
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList(FeatureFlags.TRANSFERS));

        return user;
    }

    private SignableOperationRepository mockSignableOperationRepository() {
        SignableOperationRepository signableOperationRepository = mock(SignableOperationRepository.class);

        when(signableOperationRepository.findAllByUserId(any(String.class)))
                .thenReturn(signableOperations);
        when(signableOperationRepository.findAllByUserIdAndType(USER_ID, SignableOperationTypes.TRANSFER))
                .thenReturn(signableOperations);

        return signableOperationRepository;
    }

    private ImmutableMap<String, Currency> getCurrenciesByCode() {
        List<Currency> currencies = Lists.newArrayList(new Currency("SEK", "kr", false, 10));
        return Maps.uniqueIndex(currencies, Currency::getCode);
    }

    private void storeInCache(Transfer transfer) {
        when(cacheClient.get(CacheScope.TRANSFER_BY_HASH, transfer.getHash()))
                .thenReturn(UUIDUtils.toTinkUUID(transfer.getId()));
    }

    private Transfer createBasicTransfer() {
        Transfer transfer = new Transfer();
        transfer.setUserId(UUIDUtils.fromTinkUUID(USER_ID));
        transfer.setAmount(Amount.inSEK(150.00));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(SHB_IDENTIFIER);
        transfer.setDestination(NORDEA_SSN_IDENTIFIER);
        transfer.setDueDate(DateUtils.getNextBusinessDay(new Date()));

        return transfer;
    }

    private Transfer createExecutedTransfer(TransferType type) {
        Transfer transfer = createBasicTransfer();
        transfer.setType(type);

        signableOperations.add(SignableOperation.create(transfer, SignableOperationStatuses.EXECUTED));

        return transfer;
    }

    private TransferEvent createExecutedTransferEvent(TransferType transferType, Amount amount) {
        Transfer transfer = createBasicTransfer();
        transfer.setType(transferType);

        transfer.setAmount(amount);
        SignableOperation signableOperation = SignableOperation.create(transfer, SignableOperationStatuses.EXECUTED);
        TransferEvent transferEvent = new TransferEvent(null, transfer, signableOperation, Optional.empty());

        signableOperations.add(signableOperation);

        return transferEvent;
    }

    private Transfer createValidTransfer() {
        Transfer transfer = createBasicTransfer();
        transfer.setId(UUIDUtils.fromTinkUUID("2a4a3f01038d45879037e41d7d58322e"));

        return transfer;
    }

    private void expect(LogMessageParametrized message, Object... parameters) {
        expect(getExpectedMessage(message, parameters), TransferValidationException.class);
    }

    private void expect(LogMessage message) {
        expect(getExpectedMessage(message), TransferValidationException.class);
    }

    private String getExpectedMessage(LogMessage message) {
        return getExpectedMessage(message.get());
    }

    private String getExpectedMessage(LogMessageParametrized message, Object... parameters) {
        return getExpectedMessage(message.with(parameters));
    }

    private String getExpectedMessage(String message) {
        return String.format("Transfer validation failed ( %s )", message);
    }

    private void expect(String message, Class<? extends Throwable> exception) {
        expectedException.expect(exception);
        expectedException.expectMessage(message);
    }
}
