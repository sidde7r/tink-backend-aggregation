package se.tink.libraries.i18n_aggregation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CatalogTest {

    @Test
    public void testFnutts() {
        String pattern = "This shouldn't fail {0}!";
        String name = "Fredrik";

        String formatted = Catalog.format(pattern, name);

        Assert.assertEquals("This shouldn't fail Fredrik!", formatted);
    }

    @Test
    public void redirectsStringFromLocalizedKeyToGetStringMethod() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(keyCaptor.capture())).thenReturn("Localized return value!");
        when(catalog.getString(any(LocalizableKey.class))).thenCallRealMethod();

        LocalizableKey localizableKey = new LocalizableKey("Localizable key!");

        String localizedValue = catalog.getString(localizableKey);

        assertThat(keyCaptor.getValue()).isEqualTo("Localizable key!");
        assertThat(localizedValue).isEqualTo("Localized return value!");
    }

    @Test
    public void redirectsStringsFromLocalizedPluralKeyToGetStringMethod() {
        ArgumentCaptor<String> singularCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pluralCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> countCaptor = ArgumentCaptor.forClass(Long.class);

        Catalog catalog = mock(Catalog.class);
        when(catalog.getPluralString(
                        singularCaptor.capture(), pluralCaptor.capture(), countCaptor.capture()))
                .thenAnswer(
                        invocationOnMock -> {
                            Object first = invocationOnMock.getArguments()[0];
                            Object second = invocationOnMock.getArguments()[1];
                            Object third = invocationOnMock.getArguments()[2];
                            return "Localized with: " + first + ", " + second + ", " + third;
                        });
        when(catalog.getPluralString(any(LocalizablePluralKey.class), any(Long.class)))
                .thenCallRealMethod();

        LocalizablePluralKey localizableKey =
                new LocalizablePluralKey("Singular key", "Plural key");

        String localizedValue = catalog.getPluralString(localizableKey, 5);

        assertThat(singularCaptor.getValue()).isEqualTo("Singular key");
        assertThat(pluralCaptor.getValue()).isEqualTo("Plural key");
        assertThat(countCaptor.getValue()).isEqualTo(5);

        assertThat(localizedValue).isEqualTo("Localized with: Singular key, Plural key, 5");
    }

    @Test
    public void formatsVarargsFromLocalizedParametrizedKey() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(keyCaptor.capture())).thenReturn("Localized {1} {0}!");
        when(catalog.getString(any(LocalizableParametrizedKey.class))).thenCallRealMethod();

        LocalizableParametrizedKey localizableKey =
                new LocalizableParametrizedKey("{0} {1} Non-localized")
                        .cloneWith("FirstArgument", "SecondArgument");

        String localizedValue = catalog.getString(localizableKey);

        assertThat(keyCaptor.getValue()).isEqualTo("{0} {1} Non-localized");

        assertThat(localizedValue).isEqualTo("Localized SecondArgument FirstArgument!");
    }

    @Test
    public void testCloneWith_storeInVariable() {
        Catalog catalog = Catalog.getCatalog("en-US");

        LocalizableParametrizedKey key =
                new LocalizableParametrizedKey("Test string: 1, 2, {0}, 4, {1}, 6");
        String expectedMessage = "Test string: 1, 2, 3, 4, 5, 6";

        LocalizableParametrizedKey clonedKeyWithParameters = key.cloneWith(3, 5);

        Assert.assertNotEquals(key, clonedKeyWithParameters);
        Assert.assertEquals(expectedMessage, catalog.getString(clonedKeyWithParameters));
    }

    @Test
    public void testCatalogGetString() {
        String swwEn = "There was a problem connecting to the bank.";
        String swwSv = "Ett problem uppstod med anslutningen till banken.";
        String swwIt = "C'è stato un problema nel connettersi alla banca.";
        Catalog catalog = new Catalog(new Locale("sv", "SE"));
        String catalogTranslation = catalog.getString(swwEn);
        Assert.assertEquals(swwSv, catalogTranslation);
        catalogTranslation = catalog.getString(new LocalizableKey(swwEn));
        Assert.assertEquals(swwSv, catalogTranslation);
        catalogTranslation = catalog.getString("Youtube went wrong.");
        Assert.assertEquals("Youtube went wrong.", catalogTranslation);
        catalog = new Catalog(new Locale("it", "IT"));
        catalogTranslation = catalog.getString(swwEn);
        Assert.assertEquals(swwIt, catalogTranslation);
        String nullString = null;
        try {
            catalog.getString(nullString);
        } catch (NullPointerException e) {
            Assert.assertEquals(e.getClass(), NullPointerException.class);
            return;
        }
        Assert.fail("NPE not caught");
    }

    @Test
    public void testCatalogGetStringForFR() {
        String swwEn = "There was a problem connecting to the bank.";
        String swwFR = "Un problème est survenu lors de la connexion à la banque.";

        Catalog catalog = new Catalog(new Locale("fr", "FR"));
        String catalogTranslation = catalog.getString(swwEn);
        Assert.assertEquals(swwFR, catalogTranslation);
    }

    @Test
    public void testCatalogGetStringForPT() {
        String swwEn = "There was a problem connecting to the bank.";
        String swwPT = "Ocorreu um problema ao conectar com o banco.";

        Catalog catalog = new Catalog(new Locale("pt", "PT"));
        String catalogTranslation = catalog.getString(swwEn);
        Assert.assertEquals(swwPT, catalogTranslation);
    }

    @Test
    public void testCatalogGetStringForDE() {
        String swwEn = "There was a problem connecting to the bank.";
        String swwDE = "Es gab ein Problem die Verbindung zu der Bank aufzubauen.";

        Catalog catalog = new Catalog(new Locale("de", "DE"));
        String catalogTranslation = catalog.getString(swwEn);
        Assert.assertEquals(swwDE, catalogTranslation);
    }

    @Test
    public void withLessThanOneSekGetStringReturnsTranslation() {
        // given
        String lessThanOneSekEn = "The transfer amount, less than 1 SEK is not supported.";
        String lessThanOneSekSv = "Överföringsbelopp på mindre än 1 kr stöds inte.";
        LocalizableKey lessThanOneSekEnKey = new LocalizableKey(lessThanOneSekEn);
        Catalog catalog = new Catalog(new Locale("sv", "SE"));

        // when
        String translatedLessThanOneSek = catalog.getString(lessThanOneSekEn);
        String translatedLessThanOneSekKey = catalog.getString(lessThanOneSekEnKey);

        // then
        Assert.assertEquals(lessThanOneSekSv, translatedLessThanOneSek);
        Assert.assertEquals(lessThanOneSekSv, translatedLessThanOneSekKey);
    }

    @Test
    public void testCatalogReferToAggregationTranslation() {
        // This is to verify tink-backend-aggragation is using his own po files instead of the ones
        // from backend
        String translationOnlyOccurredInAggregation = "Invalid source account";
        Catalog catalog = new Catalog(new Locale("sv", "SE"));
        Assert.assertEquals(
                "Från-kontot är inte giltigt",
                catalog.getString(translationOnlyOccurredInAggregation));
        String lessThanOneSekEn = "The transfer amount, less than 1 SEK is not supported.";
        String lessThanOneSekSv = "Överföringsbelopp på mindre än 1 kr stöds inte.";
        Assert.assertEquals(lessThanOneSekSv, catalog.getString(lessThanOneSekEn));
        Assert.assertEquals(
                lessThanOneSekSv, catalog.getString(new LocalizableKey(lessThanOneSekEn)));
    }

    @Test
    public void testEnumCloneWith() {
        Catalog catalog = Catalog.getCatalog("en-US");

        LocalizableParametrizedKey clonedKeyWithParameters = Message.MSG1.cloneWith(2, 5);

        Assert.assertNotEquals(Message.MSG1.getKey(), clonedKeyWithParameters);
        Assert.assertEquals(
                "Enum test: 1, 2, 3, 4, 5, 6", catalog.getString(clonedKeyWithParameters));
    }

    private enum Message implements LocalizableParametrizedEnum {
        MSG1(new LocalizableParametrizedKey("Enum test: 1, {0}, 3, 4, {1}, 6"));

        private LocalizableParametrizedKey key;

        Message(LocalizableParametrizedKey key) {
            this.key = key;
        }

        @Override
        public LocalizableParametrizedKey getKey() {
            return key;
        }

        @Override
        public LocalizableParametrizedKey cloneWith(Object... parameters) {
            return key.cloneWith(parameters);
        }
    }
}
