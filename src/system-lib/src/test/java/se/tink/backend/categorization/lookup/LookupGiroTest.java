package se.tink.backend.categorization.lookup;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.common.repository.cassandra.GiroRepository;
import se.tink.backend.common.utils.giro.lookup.BankGiroCrawler;
import se.tink.backend.common.utils.giro.lookup.LookupGiro;
import se.tink.backend.common.utils.giro.lookup.LookupGiroException;
import se.tink.backend.common.utils.giro.lookup.PlusGiroCrawler;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.backend.core.giros.Giro;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LookupGiroTest {
    public static class Common {
        private GiroRepository giroRepository;
        private BankGiroCrawler bankGiroCrawler;
        private PlusGiroCrawler plusGiroCrawler;

        @Before
        public void setup() {
            giroRepository = mockEmptyGiroRepository();
            bankGiroCrawler = mockBankGiroCrawlerNoResult();
            plusGiroCrawler = mockPlusGiroCrawlerNoResult();
        }

        @Test(expected = LookupGiroException.class)
        public void nonValidLuhnThrows() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            try {
                lookupGiro.lookup("1234");
            } catch (LookupGiroException exception) {
                assertThat(exception.getType()).isEqualTo(LookupGiroException.Type.INVALID_FORMAT);
                throw exception;
            }
        }

        @Test(expected = LookupGiroException.class)
        public void severalNumberOfDashesIsNotValid() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            try {
                lookupGiro.lookup("5862-808-2");
            } catch (LookupGiroException exception) {
                assertThat(exception.getType()).isEqualTo(LookupGiroException.Type.INVALID_FORMAT);
                throw exception;
            }
        }

        @Test(expected = LookupGiroException.class)
        public void accountNumberCannotContainOtherThanNumbersAndADash() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            try {
                lookupGiro.lookup("abc5862808-2");
            } catch (LookupGiroException exception) {
                assertThat(exception.getType()).isEqualTo(LookupGiroException.Type.INVALID_FORMAT);
                throw exception;
            }
        }

        @Test
        public void onlyPGValidNumberReturnsOnlyPG() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            List<AccountIdentifier> lookup = lookupGiro.lookup("5862808-2");

            FluentIterable<AccountIdentifier.Type> lookupTypes = FluentIterable
                    .from(lookup)
                    .transform(TO_TYPE);

            assertThat(lookupTypes).hasSize(1);
            assertThat(lookupTypes).contains(AccountIdentifier.Type.SE_PG);
        }

        @Test
        public void onlyBGValidNumberReturnsOnlyBG() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            List<AccountIdentifier> lookup = lookupGiro.lookup("5862-8082");

            FluentIterable<AccountIdentifier.Type> lookupTypes = FluentIterable
                    .from(lookup)
                    .transform(TO_TYPE);

            assertThat(lookupTypes).hasSize(1);
            assertThat(lookupTypes).contains(AccountIdentifier.Type.SE_BG);
        }

        @Test
        public void bothPGBGValidReturnsBoth() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);
            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            FluentIterable<AccountIdentifier.Type> lookupTypes = FluentIterable
                    .from(lookup)
                    .transform(TO_TYPE);

            assertThat(lookupTypes).hasSize(2);
            assertThat(lookupTypes).contains(AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);
        }

        private static final Function<AccountIdentifier, AccountIdentifier.Type> TO_TYPE =
                AccountIdentifier::getType;
    }

    public static class NotInDatabase {
        private GiroRepository giroRepository;

        @Before
        public void setup() {
            giroRepository = mockEmptyGiroRepository();
        }

        @Test
        public void whenCrawlingBothPGBGResult() throws LookupGiroException {
            BankGiroCrawler bankGiroCrawler = mockBankGiroCrawlerReturning("9020900");
            PlusGiroCrawler plusGiroCrawler = mockPlusGiroCrawlerReturning("9020900");
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Crawled BG Name"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Crawled PG Name"));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }

        @Test
        public void whenCrawlingOnlyPGResult_HasAlsoEmptyIdentifierForUnknown() throws LookupGiroException {
            BankGiroCrawler bankGiroCrawler = mockBankGiroCrawlerNoResult();
            PlusGiroCrawler plusGiroCrawler = mockPlusGiroCrawlerReturning("9020900");
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", null),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Crawled PG Name"));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }

        @Test
        public void whenCrawlingOnlyBGResult_HasAlsoEmptyIdentifierForUnknown() throws LookupGiroException {
            BankGiroCrawler bankGiroCrawler = mockBankGiroCrawlerReturning("9020900");
            PlusGiroCrawler plusGiroCrawler = mockPlusGiroCrawlerNoResult();
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Crawled BG Name"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", null));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }
    }

    public static class OneInDatabase {
        private BankGiroCrawler bankGiroCrawler;
        private PlusGiroCrawler plusGiroCrawler;

        @Before
        public void setup() {
            bankGiroCrawler = mockBankGiroCrawlerReturning("9020900");
            plusGiroCrawler = mockPlusGiroCrawlerReturning("9020900");
        }

        @Test
        public void whenBGInDatabase_CrawlsOnlyPG() throws LookupGiroException {
            GiroRepository giroRepository = mockGiroRepositoryReturningOnlyBG("9020900");
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Repository BG Name"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Crawled PG Name"));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }

        @Test
        public void whenPGInDatabase_CrawlsOnlyBG() throws LookupGiroException {
            GiroRepository giroRepository = mockGiroRepositoryReturningOnlyPG("9020900");
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Crawled BG Name"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Repository PG Name"));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }

        @Test
        public void whenBothInDatabase_OnlyUsesFromDatabase() throws LookupGiroException {
            GiroRepository giroRepository = mockGiroRepositoryReturningPGBG("9020900");
            LookupGiro lookupGiro = new LookupGiro(giroRepository, bankGiroCrawler, plusGiroCrawler);

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Repository BG Name"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Repository PG Name"));
            assertThat(lookup).hasSize(expectedIdentifiers.size());
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }
    }

    public static class WithRealBGPGCrawlers {
        private GiroRepository giroRepository;

        @Before
        public void setup() {
            giroRepository = mockEmptyGiroRepository();
        }

        @Test
        @Ignore
        public void lookupBarncancerfondenPGBG() throws LookupGiroException {
            LookupGiro lookupGiro = new LookupGiro(giroRepository, new BankGiroCrawler(), new PlusGiroCrawler());

            List<AccountIdentifier> lookup = lookupGiro.lookup("9020900");

            List<AccountIdentifier> expectedIdentifiers = ImmutableList.of(
                    AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900", "Barncancerfonden"),
                    AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900", "Barncancerfonden/barn"));

            assertThat(lookup).hasSize(2);
            assertThat(lookup).usingFieldByFieldElementComparator().containsAll(expectedIdentifiers);
        }

    }

    private static GiroRepository mockGiroRepositoryReturningPGBG(String identifier) {
        GiroRepository mockedRepository = mockGiroRepositoryBase();

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_PG))
                .thenReturn(Optional.of(
                        AccountIdentifier.create(AccountIdentifier.Type.SE_PG, identifier, "Repository PG Name")));

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_BG))
                .thenReturn(Optional.of(
                        AccountIdentifier.create(AccountIdentifier.Type.SE_BG, identifier, "Repository BG Name")));

        return mockedRepository;
    }

    private static GiroRepository mockGiroRepositoryReturningOnlyPG(String identifier) {
        GiroRepository mockedRepository = mockGiroRepositoryBase();

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_PG))
                .thenReturn(Optional.of(
                        AccountIdentifier.create(AccountIdentifier.Type.SE_PG, identifier, "Repository PG Name")));

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_BG))
                .thenReturn(Optional.empty());

        return mockedRepository;
    }

    private static GiroRepository mockGiroRepositoryReturningOnlyBG(String identifier) {
        GiroRepository mockedRepository = mockGiroRepositoryBase();

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_BG))
                .thenReturn(Optional.of(
                        AccountIdentifier.create(AccountIdentifier.Type.SE_BG, identifier, "Repository BG Name")));

        when(mockedRepository.getIdentifierFor(identifier, AccountIdentifier.Type.SE_PG))
                .thenReturn(Optional.empty());

        return mockedRepository;
    }

    private static GiroRepository mockEmptyGiroRepository() {
        GiroRepository mockedRepository = mockGiroRepositoryBase();

        when(mockedRepository.getIdentifierFor(any(String.class), any(AccountIdentifier.Type.class)))
                .thenReturn(Optional.empty());

        return mockedRepository;
    }

    private static GiroRepository mockGiroRepositoryBase() {
        GiroRepository mockedRepository = mock(GiroRepository.class);

        when(mockedRepository.save(any(Giro.class))).thenAnswer(
                invocationOnMock -> (Giro) invocationOnMock.getArguments()[0]);

        return mockedRepository;
    }

    private static BankGiroCrawler mockBankGiroCrawlerNoResult() {
        BankGiroCrawler mock = mock(BankGiroCrawler.class);
        when(mock.find(any(String.class)))
                .thenReturn(Optional.empty());
        return mock;
    }

    private static BankGiroCrawler mockBankGiroCrawlerReturning(String identifier) {
        BankGiroCrawler mock = mock(BankGiroCrawler.class);

        BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(identifier);
        bankGiroIdentifier.setName("Crawled BG Name");

        when(mock.find(identifier))
                .thenReturn(Optional.<AccountIdentifier>of(bankGiroIdentifier));

        return mock;
    }

    private static PlusGiroCrawler mockPlusGiroCrawlerNoResult() {
        PlusGiroCrawler mock = mock(PlusGiroCrawler.class);
        when(mock.find(any(String.class)))
                .thenReturn(Optional.empty());
        return mock;
    }

    private static PlusGiroCrawler mockPlusGiroCrawlerReturning(String identifier) {
        PlusGiroCrawler mock = mock(PlusGiroCrawler.class);

        PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(identifier);
        plusGiroIdentifier.setName("Crawled PG Name");

        when(mock.find(identifier))
                .thenReturn(Optional.<AccountIdentifier>of(plusGiroIdentifier));

        return mock;
    }
}
