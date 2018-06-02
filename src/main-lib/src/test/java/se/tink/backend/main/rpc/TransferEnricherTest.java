package se.tink.backend.main.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferEnricherException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;

public class TransferEnricherTest {
    private TransferDestinationPatternRepository patternRepository;
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private FraudDetailsRepository fraudDetailsRepository;

    private static final AccountIdentifier DEFAULT_SOURCE_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "8492093829");
    private static final AccountIdentifier DEFAULT_DESTINATION_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "8381063");
    private static final AccountIdentifier SWEDISH_SHB_IDENTIFIER = new SwedishSHBInternalIdentifier("135858358");
    private static final AccountIdentifier FINISH_IDENTIFIER = new FinnishIdentifier("6152135858358");
    private static final AccountIdentifier NORDEA_SSN_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537");
    private static final AccountIdentifier SHB_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "6152135538858");
    private static final AccountIdentifier SHB_IDENTIFIER_WITH_NAME = AccountIdentifier.create(AccountIdentifier.Type.SE, "6152135538858", "Johannes");
    private static final AccountIdentifier BARNCANCERFONDEN_BG = AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900");
    private static final AccountIdentifier BARNCANCERFONDEN_PG = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900");
    private static final AccountIdentifier TINK_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.TINK, "889497218970431ab77b22e07368bdee");

    private static final AccountIdentifier NOT_OWNED_DESTINATION_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "6000987654321");
    private static final AccountIdentifier OWNED_DESTINATION_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "6000123456789");

    private static final String USER_ID = "627fba23f10a4eb9bb667be6f144151f";
    private static final String CREDENTIALS_ID = "627fba23f10a4eb9bb667be6f144152f";

    private Transfer transfer;
    private User user;
    private Credentials credentials;
    private Catalog catalog;

    private TransferEnricher enricher;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        user = createUser();
        transfer = createValidTransfer();
        catalog = Catalog.getCatalog(user.getLocale());

        mockCredentialsRepository();
        mockAccountRepository();
        mockTransferDestinationPatternRepository();
        mockFraudDetailsRepository();

        enricher = new TransferEnricher(patternRepository, accountRepository, credentialsRepository, fraudDetailsRepository);
    }

    @Test
    public void ensureExceptionIsThrown_whenTransferDestinationPatternRepositoryIsNull_whileInstantiatingEnricher() {
        expect("No TransferDestinationPatternRepository provided", InstantiationException.class);

        new TransferEnricher(null, accountRepository, credentialsRepository, fraudDetailsRepository);
    }

    @Test
    public void ensureExceptionIsThrown_whenAccountRepositoryIsNull_whileInstantiatingEnricher() {
        expect("No AccountRepository provided", InstantiationException.class);

        new TransferEnricher(patternRepository, null, credentialsRepository, fraudDetailsRepository);
    }

    @Test
    public void ensureExceptionIsThrown_whenCredentialsRepositoryIsNull_whileInstantiatingEnricher() {
        expect("No CredentialsRepository provided", InstantiationException.class);

        new TransferEnricher(patternRepository, accountRepository, null, fraudDetailsRepository);
    }

    @Test
    public void ensureExceptionIsThrown_whenFraudDetailsRepositoryIsNull_whileInstantiatingEnricher() {
        expect("No FraudDetailsRepository provided", InstantiationException.class);

        new TransferEnricher(patternRepository, accountRepository, credentialsRepository, null);
    }

    @Test
    public void ensureEnricherIsInstantiated_whenNoInjectedObjectsAreNull() {
        new TransferEnricher(patternRepository, accountRepository, credentialsRepository, fraudDetailsRepository);
    }

    @Test
    public void ensureEnrichAndGetCredentials_throwsBadRequest_whenTransfer_isNull() throws TransferEnricherException, TransferValidationException {
        expect(TransferNotFoundException.MESSAGE, TransferNotFoundException.class);

        enricher.enrichAndGetCredentials(null, catalog);
    }

    @Test
    public void ensureEnrichAndGetCredentials_throwsException_whenUserId_isNull() throws TransferEnricherException, TransferValidationException {
        expect(LogMessage.MISSING_USER_ID.get(), TransferValidationException.class);

        transfer.setUserId(null);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureExceptionIsThrown_whenSourceAccount_wasNotFound() throws TransferEnricherException, TransferValidationException {
        expect("Source account should be present", IllegalStateException.class);

        when(accountRepository.findByUserId(any(String.class))).thenReturn(null);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureFindCredentialsFor_account_throwsException_whenCredentialsId_isNull() throws TransferEnricherException, TransferValidationException {
        expect(LogMessage.MISSING_CREDENTIALS_ID);

        Account accountWithoutCredentials = new Account();
        accountWithoutCredentials.putIdentifier(DEFAULT_SOURCE_IDENTIFIER);

        when(accountRepository.findByUserId(any(String.class)))
                .thenReturn(Collections.singletonList(accountWithoutCredentials));

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureFindCredentialsFor_account_throwsException_whenCredentialsNotFoundInDB() throws TransferEnricherException, TransferValidationException {
        expect(LogMessage.NOT_FOUND_CREDENTIALS);

        when(credentialsRepository.findOne(any(String.class))).thenReturn(null);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhenAccountId_isNull()  throws TransferEnricherException, TransferValidationException {
        ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhenAccountId_is(null);
    }

    @Test
    public void ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhenAccountId_isEmpty() throws TransferEnricherException, TransferValidationException {
        ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhenAccountId_is("");
    }

    private void ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhenAccountId_is(String nullOrEmpty) throws TransferEnricherException, TransferValidationException {
        expect(LogMessage.MISSING_ACCOUNT_ID);

        Account sourceAccount = createAccount(transfer.getSource());
        sourceAccount.setId(nullOrEmpty);

        when(accountRepository.findByUserId(any(String.class)))
                .thenReturn(Collections.singletonList(sourceAccount));

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhen_DBReturnEmptyList() throws TransferEnricherException, TransferValidationException {
        ensureExceptionIsThrown_whenTransferDestinationPatternRepositoryReturn(
                Lists.<TransferDestinationPattern>newArrayList());
    }

    @Test
    public void ensureFindTransferDestinationPatternsFor_account_throwsExceptionWhen_DBReturnNull() throws TransferEnricherException, TransferValidationException {
        ensureExceptionIsThrown_whenTransferDestinationPatternRepositoryReturn(null);
    }

    private void ensureExceptionIsThrown_whenTransferDestinationPatternRepositoryReturn(
            List<TransferDestinationPattern> nullOrEmpty)  throws TransferEnricherException, TransferValidationException {

        expect(LogMessage.NOT_FOUND_PATTERNS);

        when(patternRepository.findAllByUserIdAndAccountId(any(String.class), any(String.class)))
                .thenReturn(nullOrEmpty);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenDestinationIsNull() throws TransferEnricherException, TransferValidationException{
        transfer.setDestination(null);
        expect(LogMessage.MISSING_DESTINATION);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenDestinationIsInvalid() throws TransferEnricherException, TransferValidationException {
        transfer = createTransferWithInvalidDestination();

        expect(LogMessage.INVALID_DESTINATION);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenTinkAccountNotFound() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(TINK_IDENTIFIER);
        expect(LogMessage.NOT_FOUND_ACCOUNT);

        when(accountRepository.findOne(any(String.class))).thenReturn(null);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenIdentifiersNotFoundOnTinkAccount() throws TransferEnricherException, TransferValidationException {
        Account accountWithoutIdentifiers = createAccount();

        transfer.setDestination(TINK_IDENTIFIER);
        expect(LogMessage.NOT_FOUND_ACCOUNT_IDENTIFIERS);

        when(accountRepository.findOne(any(String.class))).thenReturn(accountWithoutIdentifiers);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenNoAccountIdentifierMatchesDestinationIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(TINK_IDENTIFIER);
        expect(LogMessage.NO_MATCH_ACCOUNTS_PATTERNS);

        when(accountRepository.findOne(any(String.class)))
                .thenReturn(createAccount(TINK_IDENTIFIER, SWEDISH_SHB_IDENTIFIER, FINISH_IDENTIFIER));

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineDestinationIdentifier_returnAccountIdentifier_whenTinkAccountIdentifier_matchesDestinationIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(TINK_IDENTIFIER);

        final AccountIdentifier finalDestinationIdentifier = NORDEA_SSN_IDENTIFIER;

        when(accountRepository.findOne(any(String.class)))
                .thenReturn(createAccount(TINK_IDENTIFIER, finalDestinationIdentifier));

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(transfer.getDestination(), finalDestinationIdentifier);
    }

    @Test
    public void ensureRefineDestinationIdentifier_throwsException_whenPatternsDoesNotMatchDestinationIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(FINISH_IDENTIFIER);

        expect(LogMessage.NO_MATCH_IDENTIFIER_PATTERNS);

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void ensureRefineSourceIdentifier_throwsException_whenFoundIdentifier_isNull() throws TransferEnricherException, TransferValidationException {
        expect(LogMessage.NOT_FOUND_PREFERRED_SOURCE_IDENTIFIER);

        Account sourceAccount = mock(Account.class);
        when(sourceAccount.getCredentialsId()).thenReturn(CREDENTIALS_ID);
        when(sourceAccount.getId()).thenReturn(USER_ID);
        when(sourceAccount.definedBy(any(AccountIdentifier.class))).thenReturn(true);
        when(sourceAccount.getPreferredIdentifier(transfer.getDestination().getType()))
                .thenReturn(null);

        when(accountRepository.findByUserId(any(String.class))).thenReturn(Collections.singletonList(sourceAccount));

        enricher.enrichAndGetCredentials(transfer, catalog);
    }

    @Test
    public void testValidEnricherWorkflow() throws TransferEnricherException, TransferValidationException {
        Credentials foundCredentials = enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(transfer.getSource(), DEFAULT_SOURCE_IDENTIFIER);
        Assert.assertEquals(transfer.getDestination(), DEFAULT_DESTINATION_IDENTIFIER);
        Assert.assertEquals(transfer.getCredentialsId(), UUIDUtils.fromTinkUUID(credentials.getId()));
        Assert.assertEquals(transfer.getUserId(), UUIDUtils.fromTinkUUID(user.getId()));
        Assert.assertEquals(credentials, foundCredentials);
    }

    @Test
    public void testValidEnricherWorkflow_withTinkDestinationIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(TINK_IDENTIFIER);

        when(accountRepository.findOne(any(String.class)))
                .thenReturn(createAccount(TINK_IDENTIFIER, NORDEA_SSN_IDENTIFIER));

        Credentials foundCredentials = enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(transfer.getSource(), DEFAULT_SOURCE_IDENTIFIER);
        Assert.assertEquals(transfer.getDestination(), NORDEA_SSN_IDENTIFIER);
        Assert.assertEquals(transfer.getCredentialsId(), UUIDUtils.fromTinkUUID(credentials.getId()));
        Assert.assertEquals(transfer.getUserId(), UUIDUtils.fromTinkUUID(user.getId()));
        Assert.assertEquals(credentials, foundCredentials);
    }

    @Test
    public void testValidEnricherWorkflow_withBgIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(BARNCANCERFONDEN_BG);

        Credentials foundCredentials = enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(transfer.getSource(), DEFAULT_SOURCE_IDENTIFIER);
        Assert.assertEquals(transfer.getDestination(), BARNCANCERFONDEN_BG);
        Assert.assertEquals(transfer.getCredentialsId(), UUIDUtils.fromTinkUUID(credentials.getId()));
        Assert.assertEquals(transfer.getUserId(), UUIDUtils.fromTinkUUID(user.getId()));
        Assert.assertEquals(credentials, foundCredentials);
    }

    @Test
    public void testValidEnricherWorkflow_withPgIdentifier() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(BARNCANCERFONDEN_PG);

        Credentials foundCredentials = enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(transfer.getSource(), DEFAULT_SOURCE_IDENTIFIER);
        Assert.assertEquals(transfer.getDestination(), BARNCANCERFONDEN_PG);
        Assert.assertEquals(transfer.getCredentialsId(), UUIDUtils.fromTinkUUID(credentials.getId()));
        Assert.assertEquals(transfer.getUserId(), UUIDUtils.fromTinkUUID(user.getId()));
        Assert.assertEquals(credentials, foundCredentials);
    }

    @Test
    public void testDestinationMessageIsEnrichedWithGivenAndLastName() throws TransferEnricherException, TransferValidationException {
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Lists.newArrayList(createAccount(DEFAULT_SOURCE_IDENTIFIER)));

        transfer.setDestination(SHB_IDENTIFIER);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals("Given Last", transfer.getDestinationMessage());
    }

    @Test
    public void testDestinationMessageIsEnrichedWithSourceAccountName_whenDestinationIsOwnedByUser() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(OWNED_DESTINATION_IDENTIFIER);
        transfer.setSource(SHB_IDENTIFIER);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals("From Source Account", transfer.getDestinationMessage());
    }

    @Test
    public void testDestinationMessageDefaultsToFormattedAccountNumber() throws TransferEnricherException, TransferValidationException {
        when(fraudDetailsRepository.findAllByUserIdAndType(anyString(),
                eq(FraudDetailsContentType.IDENTITY))).thenReturn(Lists.<FraudDetails>newArrayList());

        transfer.setDestination(NOT_OWNED_DESTINATION_IDENTIFIER);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(DEFAULT_SOURCE_IDENTIFIER.getIdentifier(new DisplayAccountIdentifierFormatter()), transfer.getDestinationMessage());
    }

    @Test
    public void testSourceMessageIsEnrichedWithDestinationAccountName() throws TransferEnricherException, TransferValidationException {
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Lists.newArrayList(createAccount(DEFAULT_SOURCE_IDENTIFIER)));

        transfer.setDestination(SHB_IDENTIFIER_WITH_NAME);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals("Johannes", transfer.getSourceMessage());
    }

    @Test
    public void testSourceMessageIsEnrichedWithDestinationAccountName_whenDestinationIsOwnedByUser() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(OWNED_DESTINATION_IDENTIFIER);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals("To Owned Destination Account", transfer.getSourceMessage());
    }

    @Test
    public void testSourceMessageDefaultsToFormattedAccountNumber() throws TransferEnricherException, TransferValidationException {
        transfer.setDestination(NOT_OWNED_DESTINATION_IDENTIFIER);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(NOT_OWNED_DESTINATION_IDENTIFIER.getIdentifier(new DisplayAccountIdentifierFormatter()), transfer.getSourceMessage());
    }

    @Test
    public void ensureOcrIsAddedAsDestinationMessage_whenPresentInGiroIdentifier() throws TransferEnricherException, TransferValidationException {
        String giroNumber = "9020900";
        String ocr = "1212121212";
        transfer.setDestination(new BankGiroIdentifier(giroNumber, ocr));
        transfer.setType(TransferType.PAYMENT);

        enricher.enrichAndGetCredentials(transfer, catalog);
        Assert.assertEquals(ocr, transfer.getDestinationMessage());
        Assert.assertEquals(giroNumber, transfer.getDestination().getIdentifier(new DefaultAccountIdentifierFormatter()));
        Assert.assertEquals("902-0900", transfer.getDestination().getIdentifier(new DisplayAccountIdentifierFormatter()));

        transfer.setDestination(new PlusGiroIdentifier(giroNumber, ocr));

        enricher.enrichAndGetCredentials(transfer, catalog);
        Assert.assertEquals(ocr, transfer.getDestinationMessage());
        Assert.assertEquals(giroNumber, transfer.getDestination().getIdentifier(new DefaultAccountIdentifierFormatter()));
        Assert.assertEquals("902090-0", transfer.getDestination().getIdentifier(new DisplayAccountIdentifierFormatter()));
    }

    @Test
    public void ensureDueDateIsPopulated_withNextBusinessDay_ifDueDateIsNull_andDestinationIdentifier_isGiroWithOcr() throws TransferEnricherException, TransferValidationException {
        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900/1212121212"));
        transfer.setDueDate(null);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(DateUtils.getNextBusinessDay(), transfer.getDueDate());
    }

    @Test
    public void ensureDueDateIsNotChanged_whenDueDateIsNotNull_andDestinationIdentifier_isGiroWithOcr() throws TransferEnricherException, TransferValidationException {
        Date dueDate = DateUtils.getFutureBusinessDay(new Date(), 15);

        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900/1212121212"));
        transfer.setDueDate(dueDate);

        enricher.enrichAndGetCredentials(transfer, catalog);

        Assert.assertEquals(dueDate, transfer.getDueDate());
    }

    private Transfer createTransferWithInvalidDestination() {
        String json = "{\"amount\":150.0,\"credentialsId\":null,\"currency\":\"SEK\",\"destinationMessage\":null,\"id\":\"2a4a3f01-038d-4587-9037-e41d7d58322e\",\"sourceMessage\":null,\"userId\":\"627fba23-f10a-4eb9-bb66-7be6f144151f\",\"type\":\"BANK_TRANSFER\",\"dueDate\":1469618176470,\"payloadSerialized\":null,\"destinationUri\":\"se-bg://0\",\"sourceUri\":\"se://6152135538858\"}";

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json, Transfer.class);
        } catch (IOException e) {
            throw new AssertionError("Couldn't deserialize json transfer");
        }
    }

    private void mockAccountRepository() {
        accountRepository = mock(AccountRepository.class);

        when(accountRepository.findByUserId(any(String.class)))
                .thenReturn(createAccountsWith(transfer.getSource()));
    }

    private void mockTransferDestinationPatternRepository() {
        patternRepository = mock(TransferDestinationPatternRepository.class);
        List<TransferDestinationPattern> patterns = createPatterns(AccountIdentifier.Type.SE,
                AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);

        when(patternRepository.findAllByUserIdAndAccountId(any(String.class), any(String.class)))
                .thenReturn(patterns);
    }

    private void mockCredentialsRepository() {
        credentialsRepository = mock(CredentialsRepository.class);
        credentials = createCredentials();

        when(credentialsRepository.findOne(any(String.class))).thenReturn(credentials);
    }

    private void mockFraudDetailsRepository() {
        fraudDetailsRepository = mock(FraudDetailsRepository.class);

        FraudDetails details1 = new FraudDetails();
        FraudDetails details2 = new FraudDetails();
        details1.setDate(DateUtils.addDays(new Date(), -10)); //older
        details2.setDate(new Date());

        FraudIdentityContent content = new FraudIdentityContent();
        content.setFirstName("First Given");
        content.setGivenName("Given");
        content.setLastName("Last");
        details2.setContent(content);

        when(fraudDetailsRepository.findAllByUserIdAndType(anyString(),
                eq(FraudDetailsContentType.IDENTITY))).thenReturn(Lists.newArrayList(details1, details2));
    }

    private List<Account> createAccountsWith(AccountIdentifier identifier) {
        List<Account> accounts = createAccounts();
        accounts.add(createAccount(identifier));

        return accounts;
    }

    private Credentials createCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(CREDENTIALS_ID);
        credentials.setUserId(user.getId());

        return credentials;
    }

    private List<Account> createAccounts() {
        List<Account> accounts = Lists.newArrayList();

        accounts.add(createAccount(NORDEA_SSN_IDENTIFIER));

        Account possibleSource = createAccount(SHB_IDENTIFIER);
        Account possibleDestination = createAccount(OWNED_DESTINATION_IDENTIFIER);
        possibleSource.setName("Source Account");
        possibleDestination.setName("Owned Destination Account");

        accounts.add(possibleSource);
        accounts.add(possibleDestination);

        return accounts;
    }

    private Account createAccount(AccountIdentifier... identifiers) {
        Account account = new Account();
        account.setType(AccountTypes.CHECKING);
        account.setCredentialsId(credentials.getId());

        for (AccountIdentifier identifier : identifiers) {
            account.putIdentifier(identifier);
        }

        return account;
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        UserProfile profile = new UserProfile();
        profile.setLocale("en_US");
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList(FeatureFlags.TRANSFERS));

        return user;
    }

    private Transfer createValidTransfer() {
        Transfer transfer = new Transfer();
        transfer.setUserId(UUIDUtils.fromTinkUUID(USER_ID));
        transfer.setAmount(Amount.inSEK(150.00));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(DEFAULT_SOURCE_IDENTIFIER);
        transfer.setDestination(DEFAULT_DESTINATION_IDENTIFIER);
        transfer.setDueDate(DateUtils.getNextBusinessDay(new Date()));

        return transfer;
    }

    private List<TransferDestinationPattern> createPatterns(AccountIdentifier.Type... types) {
        List<TransferDestinationPattern> patterns = Lists.newArrayList();

        for (AccountIdentifier.Type type : types) {
            patterns.add(createPatternFor(type));
        }

        return patterns;
    }

    private TransferDestinationPattern createPatternFor(AccountIdentifier.Type type) {
        TransferDestinationPattern tdp = new TransferDestinationPattern();
        tdp.setType(type);
        tdp.setPattern(".+");

        return tdp;
    }

    private void expect(LogMessage message) {
        expect(getExpectedMessage(message), TransferEnricherException.class);
    }

    private String getExpectedMessage(LogMessage message) {
        return String.format("Failed to enrich transfer ( %s )", message.get());
    }

    private void expect(String message, Class<? extends Throwable> exception) {
        expectedException.expect(exception);
        expectedException.expectMessage(message);
    }
}
