package se.tink.backend.main.controllers;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountDetails;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.Loan;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.UserState;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.main.TestUtils;
import se.tink.backend.main.controllers.abnamro.AbnAmroCreditCardController;
import se.tink.backend.rpc.UpdateAccountRequest;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class AccountServiceControllerTest {
    @Mock AccountRepository accountRepository;
    @Mock CredentialsRepository credentialsRepository;
    @Mock LoanDataRepository loanDataRepository;
    @Mock UserStateRepository userStateRepository;
    @Mock CacheClient cacheClient;
    @Mock SystemServiceFactory systemServiceFactory;
    @Mock Supplier<ProviderImageMap> providerImageMapSupplier;
    @Mock FirehoseQueueProducer firehoseQueueProducer;
    @Mock UserRepository userRepository;
    MetricRegistry metricregistry = new MetricRegistry();
    AccountServiceController accountServiceController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockDefaultData();
        accountServiceController = new AccountServiceController(cacheClient, systemServiceFactory, accountRepository,
                credentialsRepository, loanDataRepository, userStateRepository, userRepository, firehoseQueueProducer,
                providerImageMapSupplier, Cluster.TINK, mock(AbnAmroCreditCardController.class), metricregistry);
    }

    private void mockDefaultData() {
        ProviderImageMap providerImageMap = mockProviderImageMapDefaultData();
        when(providerImageMapSupplier.get()).thenReturn(providerImageMap);
        when(systemServiceFactory.getProcessService()).thenReturn(mock(ProcessService.class));
        when(userStateRepository.findOne(eq("userId"))).thenReturn(new UserState());
        when(accountRepository.save(any(Account.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    /**
     * ProviderImageMap that returns `provider-icon` and `provider-name` ImageUrls object
     */
    private static ProviderImageMap mockProviderImageMapDefaultData() {
        ProviderImageMap providerImageMap = mock(ProviderImageMap.class);

        when(providerImageMap.getImagesForAccount(any(String.class), any(Account.class)))
                .then(invocationOnMock -> {
                    String providerName = invocationOnMock.getArgument(0);
                    ImageUrls imageUrls = new ImageUrls();
                    imageUrls.setIcon(providerName + "-icon");
                    return imageUrls;
                });

        return providerImageMap;
    }

    @Test
    public void returnEmptyListForNoAccountsInRepository() {
        when(accountRepository.findByUserId(eq("userId"))).thenReturn(Collections.<Account>emptyList());

        List<Account> accounts = accountServiceController.list("userId");

        assertNotNull("Result shouldn't be null, at least we need to return empty list", accounts);
        assertTrue("We do not expect accounts here", accounts.isEmpty());
    }

    @Test
    public void returnOrphanAccounts() {
        when(accountRepository.findByUserId(eq("userId"))).thenReturn(Arrays.asList(
                createAccount("orphanAccId", "userId", "unknownCredentialsId"),
                createAccount("orphanAcc2Id", "userId", null),
                createAccount("existedAccId", "userId", "credentialsId")));

        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credentialsId", "ProviderName").build());

        List<Account> accounts = accountServiceController.list("userId");

        int expSize = 3;

        assertNotNull("Result shouldn't be null, at least we need to return empty list", accounts);
        assertEquals("We do not expect accounts here", expSize, accounts.size());
    }

    @Test
    @Parameters({ "LOAN", "MORTGAGE" })
    public void setDescriptionOnLoanAndMortgage(AccountTypes type) {
        when(accountRepository.findByUserId(eq("userId"))).thenReturn(Collections.singletonList(
                TestUtils.createAccount("accId", "userId", "credentialsId", 1000, type)));
        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credentialsId", "Bank").build());
        when(loanDataRepository.findMostRecentOneByAccountId(eq("accId"))).thenReturn(new Loan());

        List<Account> accounts = accountServiceController.list("userId");

        int expSize = 1;
        assertNotNull("Result shouldn't be null, at least we need to return empty list", accounts);
        assertEquals("We do not expect accounts here", expSize, accounts.size());
        assertNotNull("Expect value in `details` field", accounts.get(0).getDetails());

    }

    @Test
    @Parameters({ "CHECKING", "SAVINGS", "INVESTMENT", "CREDIT_CARD", "PENSION", "EXTERNAL", "OTHER" })
    public void doNotSetDescription(AccountTypes type) {
        when(accountRepository.findByUserId(eq("userId"))).thenReturn(Collections.singletonList(
                TestUtils.createAccount("accId", "userId", "credentialsId", 1000, type)));
        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credentialsId", "Bank").build());

        List<Account> accounts = accountServiceController.list("userId");

        int expSize = 1;
        assertNotNull("Result shouldn't be null, at least we need to return empty list", accounts);
        assertEquals("We do not expect accounts here", expSize, accounts.size());
        assertNull("Do not expect value in `details` field", accounts.get(0).getDetails());
    }

    @Test
    @Parameters({ "LOAN", "MORTGAGE" })
    public void doNotSetDescriptionWithoutLoans(AccountTypes type) {
        when(accountRepository.findByUserId(eq("userId"))).thenReturn(Collections.singletonList(
                TestUtils.createAccount("accId", "userId", "credentialsId", 1000, type)));
        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credentialsId", "Bank").build());

        List<Account> accounts = accountServiceController.list("userId");

        int expSize = 1;
        assertNotNull("Result shouldn't be null, at least we need to return empty list", accounts);
        assertEquals("We do not expect accounts here", expSize, accounts.size());
        assertNull("Do not expect value in `details` field", accounts.get(0).getDetails());
    }

    @Test(expected = NoSuchElementException.class)
    public void errorOnUpdateNewAccount() {
        when(accountRepository.findOne(eq("accId"))).thenReturn(null);

        accountServiceController.update("userId", "accId", new Account());
    }

    @Test(expected = IllegalArgumentException.class)
    public void errorOnUpdateAccountWithDifferentUserId() {
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(createAccount("accId", "oldUserId", "credId"));

        accountServiceController.update("userId", "accId", new Account());
    }

    @Test
    public void saveUserStateForChangeFavoriteProperty() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        final UserState userState = new UserState("userId");
        userState.setHaveManuallyFavoredAccount(false);
        when(userStateRepository.findOne(eq("userId"))).thenReturn(userState);

        Account newAccount = oldAccount.clone();
        newAccount.setFavored(!oldAccount.isFavored());

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertEquals("Did not save new value", updatedAccount.isFavored(), updatedAccount.isFavored());
        assertTrue("Flag was not set", userState.isHaveManuallyFavoredAccount());
        verify(userStateRepository).save(userState);
    }

    @Test
    public void setFlagOnChangedName() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        oldAccount.setName("New cool account");
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        Account newAccount = oldAccount.clone();
        newAccount.setName("Very cool account");

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertEquals("Did not save new value", updatedAccount.getName(), updatedAccount.getName());
        assertTrue("Flag was not set", updatedAccount.isUserModifiedName());
    }

    @Test
    public void setFlagOnChangedType() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        Account newAccount = oldAccount.clone();
        newAccount.setType(AccountTypes.SAVINGS);

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertEquals("Did not save new value", updatedAccount.getType(), updatedAccount.getType());
        assertTrue("Flag was not set", updatedAccount.isUserModifiedType());
    }

    @Test
    public void setFavoriteFalseForExcludedAccount() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        oldAccount.setFavored(false);
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        final UserState userState = new UserState("userId");
        userState.setHaveManuallyFavoredAccount(false);
        when(userStateRepository.findOne(eq("userId"))).thenReturn(userState);

        Account newAccount = oldAccount.clone();
        newAccount.setFavored(true);
        newAccount.setExcluded(true);

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertTrue("Did not save new value", updatedAccount.isExcluded());
        assertFalse("Did not change favorite type", updatedAccount.isFavored());
        assertTrue("Flag was not set", userState.isHaveManuallyFavoredAccount());
        verify(userStateRepository).save(userState);
    }

    @Test
    public void saveOnlyModifiableFields() {
        Date now = new Date();

        Loan oldLoan = createLoan(Loan.Type.BLANCO, 0.125, 1);
        Account oldAccount = createFullAccount("accId", "accNumber", "userId", "credId", "bankId",
                AccountTypes.MORTGAGE, 10000, 123, now, "accountName", 1, "Payload", true, true, true, true, true,
                "identifier", "Transfer1", "providerName-icon", null);

        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);
        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credId", "providerName").build());
        when(loanDataRepository.findMostRecentOneByAccountId("accId")).thenReturn(oldLoan);

        Account newAccount = createFullAccount("accId", "newAccNumber", "userId", "qwerty", "qwerty",
                AccountTypes.LOAN, 90, 4321, DateUtils.addDays(now, -2), "accountChangedName", 0.4,
                "Changed Payload", false, false, false, false, false, "newIdentifier", "Transfer2", "icon2",
                createLoan(Loan.Type.MORTGAGE, 0.01, 2));

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        Account expectedAccount = createFullAccount("accId", "newAccNumber", "userId", "credId", "bankId",
                AccountTypes.LOAN, 10000, 123, now, "accountChangedName", 0.4, "Payload", false, false, true,
                true, true, "identifier", "Transfer1", "providerName-icon", createLoan(Loan.Type.BLANCO, 0.125, 1));

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertThat(updatedAccount)
                .isEqualToIgnoringGivenFields(expectedAccount, "transferDestinations", "images", "details");
        assertThat(updatedAccount.getTransferDestinations().size()).isEqualTo(1);
        assertThat(updatedAccount.getTransferDestinations().get(0))
                .isEqualToComparingFieldByField(oldAccount.getTransferDestinations().get(0));
        assertThat(updatedAccount.getImages()).isEqualToComparingFieldByField(expectedAccount.getImages());
        assertThat(updatedAccount.getDetails()).isEqualToComparingFieldByField(new AccountDetails(oldLoan));

    }

    @Test
    public void updateOnlySetField() throws CloneNotSupportedException {
        Date now = new Date();

        Loan oldLoan = createLoan(Loan.Type.BLANCO, 0.125, 1);
        Account oldAccount = createFullAccount("accId", "accNumber", "userId", "credId", "bankId",
                AccountTypes.LOAN, 10000, 123, now, "accountName", 1, "Payload", false, true, true, true, true,
                "identifier", "Transfer1", null, null);

        when(accountRepository.findOne(eq("accId"))).thenReturn(oldAccount.clone());
        when(credentialsRepository.findAllIdsAndProviderNamesByUserId(eq("userId")))
                .thenReturn(ImmutableMap.<String, String>builder().put("credId", "providerName").build());
        when(loanDataRepository.findMostRecentOneByAccountId("accId")).thenReturn(oldLoan);

        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        String newAccountNumber = "newNumber";
        double newOwnership = 0.2;
        updateRequest.setAccountNumber(newAccountNumber);
        updateRequest.setOwnership(newOwnership);

        Account updatedAccount = accountServiceController.update("userId", "accId", updateRequest);

        Account expectedAccount = oldAccount.clone();
        assertNotEquals(newAccountNumber, expectedAccount.getAccountNumber());
        expectedAccount.setAccountNumber("newNumber");
        assertNotEquals(newOwnership, expectedAccount.getOwnership(), 0.001);
        expectedAccount.setOwnership(0.2);
        ImageUrls images = new ImageUrls();
        images.setIcon("providerName-icon");
        expectedAccount.setImages(images);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertThat(updatedAccount)
                .isEqualToIgnoringGivenFields(expectedAccount, "transferDestinations", "images", "details");
        assertThat(updatedAccount.getTransferDestinations().size()).isEqualTo(1);
        assertThat(updatedAccount.getTransferDestinations().get(0))
                .isEqualToComparingFieldByField(oldAccount.getTransferDestinations().get(0));
        assertThat(updatedAccount.getImages()).isEqualToComparingFieldByField(expectedAccount.getImages());
        assertThat(updatedAccount.getDetails()).isEqualToComparingFieldByField(new AccountDetails(oldLoan));

    }

    @Test
    public void generateFullStatisticsForModifyingExclude() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        oldAccount.setExcluded(false);
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        Account newAccount = oldAccount.clone();
        newAccount.setExcluded(true);

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertTrue("Did not save new value", updatedAccount.isExcluded());
        verify(systemServiceFactory.getProcessService())
                .generateStatisticsAndActivitiesWithoutNotifications("userId", StatisticMode.FULL);
    }

    @Test
    public void generateSimpleStatisticsForModifyingOwnerships() throws CloneNotSupportedException {
        Account oldAccount = createAccount("accId", "userId", "credId");
        oldAccount.setOwnership(1.);
        when(accountRepository.findOne(eq("accId")))
                .thenReturn(oldAccount);

        Account newAccount = oldAccount.clone();
        double newOwnership = 0.33;
        newAccount.setOwnership(newOwnership);

        Account updatedAccount = accountServiceController.update("userId", "accId", newAccount);

        verify(accountRepository).save(eq(updatedAccount));
        assertNotNull("Return null account", updatedAccount);
        assertEquals("Did not save new value", newOwnership, updatedAccount.getOwnership(), 0.001);
        verify(systemServiceFactory.getProcessService())
                .generateStatisticsAndActivitiesWithoutNotifications("userId", StatisticMode.SIMPLE);
    }

    private Loan createLoan(Loan.Type type, double interestRate, int numMonthsBound) {
        Loan loan = new Loan();
        loan.setType(type);
        loan.setInterest(interestRate);
        loan.setNumMonthsBound(numMonthsBound);
        return loan;
    }

    private Account createAccount(String accountId, String userId, String credentialsId) {
        return TestUtils.createAccount(accountId, userId, credentialsId, 1000, AccountTypes.CHECKING);
    }

    private Account createFullAccount(String accId, String accNumber, String userId, String credId, String bankId,
            AccountTypes types,
            double amount, double availableCredit, Date certainDate, String name, double ownership, String payload,
            boolean isExcluded, boolean isFavored, boolean isUserModifiedExcluded, boolean isUserModifiedName,
            boolean isUserModifiedType, String identifierName, String transferDestinationName, String icon,
            Loan loan) {
        TransferDestination transferDestination = new TransferDestination();
        transferDestination.setName(transferDestinationName);
        ImageUrls imageUrls = new ImageUrls();
        imageUrls.setIcon(icon);

        Account account = TestUtils.createAccount(accId, userId, credId, amount, types);
        account.setAccountNumber(accNumber);
        account.setAvailableCredit(availableCredit);
        account.setBankId(bankId);
        account.setCertainDate(certainDate);
        account.setExcluded(isExcluded);
        account.setFavored(isFavored);
        account.setName(name);
        account.setOwnership(ownership);
        account.setPayload(payload);
        account.setUserModifiedExcluded(isUserModifiedExcluded);
        account.setUserModifiedName(isUserModifiedName);
        account.setUserModifiedType(isUserModifiedType);
        account.putIdentifier(new TinkIdentifier(identifierName));
        account.setTransferDestinations(Collections.singletonList(transferDestination));
        account.setImages(imageUrls);

        if (loan != null) {
            account.setDetails(new AccountDetails(loan));
        }

        return account;
    }
}
