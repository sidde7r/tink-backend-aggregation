package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.ProviderImage;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DestinationOfAccountTest {
    @Test
    public void testModel() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();

        DestinationOfAccount destinationOfAccount = new DestinationOfAccount(accountIdentifiers);
        destinationOfAccount.setBalance(1.0);
        destinationOfAccount.setCredentialsId("mycredentialsid");
        destinationOfAccount.setName("MyName");
        destinationOfAccount.setType(AccountTypes.CHECKING);

        Destination destination = destinationOfAccount;

        assertThat(destination.getName().get()).isEqualTo("MyName");
        assertThat(destination.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(destination.getIdentifiers().size()).isEqualTo(3);
        assertThat(destination.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.TINK);
        assertThat(destination.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("889497218970431ab77b22e07368bdee");
        assertThat(destination.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(destination.is(DestinationOfAccount.class)).isTrue();
        assertThat(destination.to(DestinationOfAccount.class).getBalance()).isEqualTo(1.0);
        assertThat(destination.to(DestinationOfAccount.class).getCredentialsId()).isEqualTo("mycredentialsid");
    }

    @Test
    public void testCopy() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();

        DestinationOfAccount destinationOfAccount = new DestinationOfAccount(accountIdentifiers);
        destinationOfAccount.setBalance(1.0);
        destinationOfAccount.setCredentialsId("mycredentialsid");
        destinationOfAccount.setName("MyName");
        destinationOfAccount.setType(AccountTypes.CHECKING);

        Destination destination = destinationOfAccount;
        Destination copyOf = destination.copyOf();

        assertThat(copyOf).isNotSameAs(destination);
        assertThat(copyOf.getIdentifiers()).isNotSameAs(destination);

        assertThat(copyOf.getName().get()).isEqualTo("MyName");
        assertThat(copyOf.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(copyOf.getIdentifiers().size()).isEqualTo(3);
        assertThat(copyOf.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.TINK);
        assertThat(copyOf.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("889497218970431ab77b22e07368bdee");
        assertThat(copyOf.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(copyOf.is(DestinationOfAccount.class)).isTrue();
        assertThat(copyOf.to(DestinationOfAccount.class).getBalance()).isEqualTo(1.0);
        assertThat(copyOf.to(DestinationOfAccount.class).getCredentialsId()).isEqualTo("mycredentialsid");
    }

    @Test
    public void testImageUrls() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiersSwedish();
        Map<String, String> providerNameByCredentialsId = createProviderNameMap();
        ProviderImageMap providerImageMap = createProviderImageMap();

        DestinationOfAccount destinationOfAccount = new DestinationOfAccount(accountIdentifiers);
        destinationOfAccount.setBalance(1.0);
        destinationOfAccount.setCredentialsId("mycredentialsid");
        destinationOfAccount.setName("MyName");
        destinationOfAccount.setType(AccountTypes.CHECKING);

        ImageUrls imageUrls = destinationOfAccount.getImageUrls(providerNameByCredentialsId, providerImageMap);

        assertThat(imageUrls.getBanner()).isEqualTo("url:banner:seb.account");
        assertThat(imageUrls.getIcon()).isEqualTo("url:icon:seb.account");
    }

    private Map<String, String> createProviderNameMap() {
        return ImmutableMap.of(
                "bogus1", "abc",
                "mycredentialsid", "seb",
                "bogus2", "def");
    }

    private static List<AccountIdentifier> createAccountIdentifiersSwedish() {
        return ImmutableList.of(
                AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233")
        );
    }

    private static List<AccountIdentifier> createAccountIdentifiers() {
        AccountIdentifier tinkIdentifier = AccountIdentifier
                .create(AccountIdentifier.Type.TINK, "889497218970431ab77b22e07368bdee");
        AccountIdentifier swedishIdentifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");
        AccountIdentifier ibanIdentifier = AccountIdentifier
                .create(AccountIdentifier.Type.IBAN, "ESSESESS/SE2250000000053680239572");

        return ImmutableList.of(tinkIdentifier, swedishIdentifier, ibanIdentifier);
    }

    public static ProviderImageMap createProviderImageMap() {
        return new ProviderImageMap(
                ImmutableList.<ProviderImage>builder()
                        .add(createProviderImage("icon:seb.account", "url:icon:seb.account"))
                        .add(createProviderImage("banner:seb.account", "url:banner:seb.account"))
                        .build(),
                new ClearingNumberBankToProviderMapImpl()
        );
    }

    private static ProviderImage createProviderImage(String code, String url) {
        ProviderImage image = new ProviderImage();
        image.setCode(code);
        image.setUrl(url);
        return image;
    }
}
