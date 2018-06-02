package se.tink.backend.main.providers.transfer;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.account.UserTransferDestination;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferSourceAccountProviderImplTest {
    private static final UUID ACC_UUID = UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954");
    private static final UUID ACC_UUID2 = UUID.fromString("def12324-87e9-440f-adeb-f9f26c182954");
    private static final UUID ACC_UUID3 = UUID.fromString("75d1256a-1013-4d19-9c4d-56a1cfe652a2");
    private static final UUID ACC_UUID4 = UUID.fromString("5bc98f5d-b571-4798-9017-9c5cd76d5d4e");

    private static final UUID USER_UUID = UUID.fromString("abc12324-87e9-440f-adeb-f9f26c182954");

    private static final AccountIdentifier PG = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "4321-8765");
    private static final AccountIdentifier BG = AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "1234-5678");
    private static final AccountIdentifier DANSKE = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");
    private static final AccountIdentifier SEB = AccountIdentifier.create(AccountIdentifier.Type.SE, "5000123123");
    private static final AccountIdentifier NORDEA_SSN = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008607015537");
    private static final AccountIdentifier NORDEA_SSN2 = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "33008210303999");
    private static final AccountIdentifier SHB_ACC = AccountIdentifier
            .create(AccountIdentifier.Type.SE, "6000123456789");


    @Test
    public void testGetSourceAccounts() {
        User user = createUser();

        TransferSourceAccountProviderImpl transferSourceAccountProvider = createProvider(user);

        List<Account> sourceAccounts = transferSourceAccountProvider.getSourceAccountsAll(user);

        assertThat(sourceAccounts).hasSize(2);

        ImmutableListMultimap<String, Account> sourceAccountsById = FluentIterable
                .from(sourceAccounts)
                .index(Account::getId);
        assertThat(sourceAccountsById.keys()).hasSize(2);

        Account acc1 = sourceAccountsById.get(ACC_UUID.toString()).get(0);
        Account acc2 = sourceAccountsById.get(ACC_UUID2.toString()).get(0);

        assertThat(acc1.getId()).isEqualTo(ACC_UUID.toString());
        assertThat(acc2.getId()).isEqualTo(ACC_UUID2.toString());

        List<TransferDestination> destinations1 = acc1.getTransferDestinations();
        assertThat(destinations1).hasSize(4);

        // Since we have both patterns and userdestinations that are duplicates,
        // we expect user destinations to be prioritized
        ImmutableListMultimap<String, TransferDestination> destinationByIdentifier = getDestinationsByIdentifier(
                destinations1);

        assertThat(destinationByIdentifier.keys()).hasSize(4);
        assertThat(destinationByIdentifier.keys()).contains(DANSKE.toURI().toString() + "?name=UTD+Name");
        assertThat(destinationByIdentifier.keys()).contains(SEB.toURI().toString() + "?name=TDP+Name1");
        assertThat(destinationByIdentifier.keys()).contains(PG.toURI().toString() + "?name=TDP+PG");
        assertThat(destinationByIdentifier.keys()).contains(BG.toURI().toString() + "?name=TDP+BG");

        TransferDestination danske = destinationByIdentifier.get(DANSKE.toURI().toString() + "?name=UTD+Name").get(0);
        assertThat(danske.getBalance()).isNull();
        assertThat(danske.getDisplayBankName()).isEqualTo("Danske Bank");
        assertThat(danske.getDisplayAccountNumber()).isEqualTo("1200-112233");
        assertThat(danske.getName()).isEqualTo("UTD Name");
        assertThat(danske.getType()).isEqualTo("EXTERNAL");

        TransferDestination seb = destinationByIdentifier.get(SEB.toURI().toString() + "?name=TDP+Name1").get(0);
        assertThat(seb.getBalance()).isNull();
        assertThat(seb.getDisplayBankName()).isEqualTo("SEB");
        assertThat(seb.getDisplayAccountNumber()).isEqualTo("5000-123123");
        assertThat(seb.getName()).isEqualTo("TDP Name1");
        assertThat(seb.getType()).isEqualTo("EXTERNAL");

        List<TransferDestination> destinations2 = acc2.getTransferDestinations();
        assertThat(destinations2).hasSize(1);

        TransferDestination nordea = destinations2.get(0);
        assertThat(nordea.getBalance()).isEqualTo(1.0);
        assertThat(nordea.getDisplayBankName()).isEqualTo("Nordea");
        assertThat(nordea.getDisplayAccountNumber()).isEqualTo("3300-8607015537");
        assertThat(nordea.getName()).isEqualTo("Acc Name1");
        assertThat(nordea.getType()).isEqualTo("CHECKING");
    }

    @Test
    public void testGetSourceAccountsOnlyPGOrBG() {
        User user = createUser();
        TransferSourceAccountProviderImpl transferSourceAccountProvider = createProvider(user);

        ImmutableSet<AccountIdentifier.Type> typeFilter = ImmutableSet
                .of(AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);
        List<Account> sourceAccounts = transferSourceAccountProvider.getSourceAccountsExplicitTypes(user, typeFilter);

        assertThat(sourceAccounts).hasSize(1);

        Account acc1 = Iterables.get(sourceAccounts, 0);
        assertThat(acc1.getId()).isEqualTo(ACC_UUID.toString());

        List<TransferDestination> destinations1 = acc1.getTransferDestinations();
        assertThat(destinations1).hasSize(2);

        ImmutableListMultimap<String, TransferDestination> destinationByIdentifier = getDestinationsByIdentifier(
                destinations1);

        assertThat(destinationByIdentifier.keys()).hasSize(2);
        assertThat(destinationByIdentifier.keys()).contains(PG.toURI().toString() + "?name=TDP+PG");
        assertThat(destinationByIdentifier.keys()).contains(BG.toURI().toString() + "?name=TDP+BG");
    }

    private ImmutableListMultimap<String, TransferDestination> getDestinationsByIdentifier(
            List<TransferDestination> destinations1) {
        return FluentIterable
                .from(destinations1)
                .index(transferDestination -> transferDestination.getUri().toString());
    }

    private TransferSourceAccountProviderImpl createProvider(User user) {
        TransferDestinationPatternProvider destinationPatternProvider = mockTransferDestinationPatternProvider(user);
        ProviderImageMap providerImages = mockProviderImageMap();
        AccountRepository accountRepository = mockAccountRepository(user);
        CredentialsRepository credentialsRepository = mockCredentialsRepository(user);
        UserTransferDestinationProvider userTransferDestinationProvider = mockUserTransferDestinationProvider(user);
        ProviderDao providerDao = mockProviderDao();

        return new TransferSourceAccountProviderImpl(
                destinationPatternProvider,
                providerImages,
                accountRepository,
                credentialsRepository,
                userTransferDestinationProvider,
                providerDao
        );
    }

    private ProviderDao mockProviderDao() {

        ProviderDao mock = mock(ProviderDao.class);

        when(mock.getProvidersByName()).thenReturn(FluentIterable.from(createProviders()).uniqueIndex(
                Provider::getName));

        return mock;
    }

    private UserTransferDestinationProvider mockUserTransferDestinationProvider(User user) {
        final UserTransferDestination destination = new UserTransferDestination();
        destination.setIdentifier(DANSKE.getIdentifier());
        destination.setName("UTD Name");
        destination.setType(DANSKE.getType());
        destination.setUserId(UUID.fromString(user.getId()));

        UserTransferDestinationProvider mock = mock(UserTransferDestinationProvider.class);

        when(mock.getDestinations(user))
                .thenAnswer(invocationOnMock -> ImmutableList.of(destination));

        return mock;
    }

    private List<Provider> createProviders() {
        List<Provider> providers = Lists.newArrayList();

        Provider p1 = new Provider();
        Provider p2 = new Provider();

        p1.setName("providerName1");
        p2.setName("providerName2");

        p1.setCapabilities(Sets.newHashSet(Provider.Capability.TRANSFERS));

        providers.add(p1);
        providers.add(p2);

        return providers;
    }

    private CredentialsRepository mockCredentialsRepository(User user) {
        Credentials credential1 = new Credentials();
        credential1.setId("CredId");
        credential1.setUserId(user.getId());
        credential1.setProviderName("providerName1");

        Credentials credential2 = new Credentials();
        credential2.setId("CredId2");
        credential2.setUserId(user.getId());
        credential2.setProviderName("providerName2");

        Credentials credential3 = new Credentials();
        credential3.setId("CredId3");
        credential3.setUserId(user.getId());
        credential3.setProviderName("providerName3");

        CredentialsRepository mock = mock(CredentialsRepository.class);

        when(mock.findAllByUserId(user.getId()))
                .thenReturn(ImmutableList.of(credential1, credential2, credential3));

        return mock;
    }

    private AccountRepository mockAccountRepository(User user) {
        Account account1 = new Account();
        account1.setId(ACC_UUID.toString());
        account1.setCredentialsId("CredId");
        account1.setBalance(1.0);
        account1.setName("Acc Name1");
        account1.setType(AccountTypes.CHECKING);
        account1.setUserId(user.getId());
        account1.putIdentifier(NORDEA_SSN);

        Account account2 = new Account();
        account2.setId(ACC_UUID2.toString());
        account2.setCredentialsId("CredId");
        account2.setBalance(1.0);
        account2.setName("Acc Name2");
        account2.setType(AccountTypes.SAVINGS);
        account2.setUserId(user.getId());
        account2.putIdentifier(NORDEA_SSN2);

        Account account3 = new Account();
        account3.setId(ACC_UUID3.toString());
        account3.setCredentialsId("CredId2");
        account3.setBalance(1.0);
        account3.setName("Acc Name3");
        account3.setType(AccountTypes.SAVINGS);
        account3.setUserId(user.getId());
        account3.putIdentifier(SHB_ACC);

        Account account4 = new Account();
        account4.setId(ACC_UUID4.toString());
        account4.setCredentialsId("CredId3");
        account4.setBalance(1.0);
        account4.setName("Acc Name4");
        account4.setType(AccountTypes.SAVINGS);
        account4.setUserId(user.getId());
        account4.putIdentifier(SHB_ACC);

        AccountRepository mock = mock(AccountRepository.class);

        when(mock.findByUserId(user.getId()))
                .thenReturn(ImmutableList.of(account1, account2, account3, account4));

        return mock;
    }

    private StringMap mockDisplayNameByProviderName() {
        StringMap mock = mock(StringMap.class);

        when(mock.get("somebank"))
                .thenReturn("Some Bank");

        return mock;
    }

    private ProviderImageMap mockProviderImageMap() {
        ImageUrls imageUrls = new ImageUrls();
        imageUrls.setBanner("somebanner");
        imageUrls.setIcon("someicon");

        ProviderImageMap mock = mock(ProviderImageMap.class);

        when(mock.getImagesForAccount(any(String.class), any(Account.class)))
                .thenReturn(imageUrls);
        when(mock.getImagesForAccount(any(String.class), any(AccountTypes.class)))
                .thenReturn(imageUrls);

        return mock;
    }

    private TransferDestinationPatternProvider mockTransferDestinationPatternProvider(User user) {
        TransferDestinationPattern singleMatch1 = TransferDestinationPattern.createForSingleMatch(
                SEB, "TDP Name1", "TDP Bank1");
        singleMatch1.setAccountId(ACC_UUID);
        TransferDestinationPattern singleMatch2 = TransferDestinationPattern.createForSingleMatch(
                DANSKE, "TDP Name2", "TDP Bank2");
        singleMatch2.setAccountId(ACC_UUID);
        TransferDestinationPattern singleMatch3 = TransferDestinationPattern.createForSingleMatch(
                NORDEA_SSN, "TDP Name3", "TDP Bank3");
        singleMatch3.setAccountId(ACC_UUID2);
        TransferDestinationPattern singleMatch4 = TransferDestinationPattern.createForSingleMatch(
                PG, "TDP PG", null);
        singleMatch4.setAccountId(ACC_UUID);
        TransferDestinationPattern singleMatch5 = TransferDestinationPattern.createForSingleMatch(
                BG, "TDP BG", null);
        singleMatch5.setAccountId(ACC_UUID);


        TransferDestinationPattern singleMatch6 = TransferDestinationPattern.createForSingleMatch(
                NORDEA_SSN, "TDP Name3", "TDP Bank3");
        singleMatch6.setAccountId(ACC_UUID3);
        TransferDestinationPattern singleMatch7 = TransferDestinationPattern.createForSingleMatch(
                NORDEA_SSN, "TDP Name3", "TDP Bank3");
        singleMatch7.setAccountId(ACC_UUID4);

        ImmutableListMultimap<String, TransferDestinationPattern> patternsByAccountId = FluentIterable
                .from(ImmutableList.of(singleMatch1, singleMatch2, singleMatch3, singleMatch4, singleMatch5, singleMatch6, singleMatch7))
                .index(p -> p.getAccountId().toString());

        TransferDestinationPatternProvider mock = mock(TransferDestinationPatternProvider.class);

        when(mock.getDestinationPatternsByAccountId(user))
                .thenReturn(patternsByAccountId);

        return mock;
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_UUID.toString());
        return user;
    }

    private abstract class StringMap implements Map<String, String> {
    }
}
