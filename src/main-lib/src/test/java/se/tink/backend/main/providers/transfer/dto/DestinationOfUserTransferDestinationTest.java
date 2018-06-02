package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderDisplayNameFinder;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DestinationOfUserTransferDestinationTest {
    @Test
    public void testModel() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();

        DestinationOfUserTransferDestination destinationOfUserTransferDestination =
                new DestinationOfUserTransferDestination(accountIdentifiers.get(0));
        destinationOfUserTransferDestination.setName("MyName");

        Destination destination = destinationOfUserTransferDestination;

        assertThat(destination.getName().get()).isEqualTo("MyName");
        assertThat(destination.getType()).isEqualTo(AccountTypes.EXTERNAL);
        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.SE);
        assertThat(destination.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("1200112233");
        assertThat(destination.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(destination.is(DestinationOfUserTransferDestination.class)).isTrue();
        assertThat(destination.to(DestinationOfUserTransferDestination.class).getDisplayBankName().get())
                .isEqualTo("Danske Bank");
    }

    @Test
    public void testCopy() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();

        DestinationOfUserTransferDestination destinationOfUserTransferDestination =
                new DestinationOfUserTransferDestination(accountIdentifiers.get(0));
        destinationOfUserTransferDestination.setName("MyName");

        Destination destination = destinationOfUserTransferDestination;
        Destination copyOf = destination.copyOf();

        assertThat(copyOf).isNotSameAs(destination);
        assertThat(copyOf.getIdentifiers()).isNotSameAs(destination);

        assertThat(copyOf.getName().get()).isEqualTo("MyName");
        assertThat(copyOf.getType()).isEqualTo(AccountTypes.EXTERNAL);
        assertThat(copyOf.getIdentifiers().size()).isEqualTo(1);
        assertThat(copyOf.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.SE);
        assertThat(copyOf.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("1200112233");
        assertThat(copyOf.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(copyOf.is(DestinationOfUserTransferDestination.class)).isTrue();
        assertThat(copyOf.to(DestinationOfUserTransferDestination.class).getDisplayBankName().get())
                .isEqualTo("Danske Bank");
    }

    @Test
    public void testImageUrls() {
        UUID randomUUID = UUID.randomUUID();
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();
        ProviderDisplayNameFinder displayNameFinder = createProviderDisplayNameFinder();
        ProviderImageMap providerImageMap = createProviderImageMap();
        ClearingNumberBankToProviderMapImpl clearingNumberBankToProviderMap = new ClearingNumberBankToProviderMapImpl();

        DestinationOfUserTransferDestination destinationOfUserTransferDestination =
                new DestinationOfUserTransferDestination(accountIdentifiers.get(0));
        destinationOfUserTransferDestination.setName("MyName");

        ImageUrls imageUrls = destinationOfUserTransferDestination.getImageUrls(
                providerImageMap);

        assertThat(imageUrls.getBanner()).isEqualTo("url:banner:danskebank");
        assertThat(imageUrls.getIcon()).isEqualTo("url:icon:danskebank");
    }

    private static List<AccountIdentifier> createAccountIdentifiers() {
        AccountIdentifier swedishIdentifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");

        return ImmutableList.of(swedishIdentifier);
    }

    private static ProviderDisplayNameFinder createProviderDisplayNameFinder() {
        Credentials credential = new Credentials();
        credential.setProviderName("danskebank");
        credential.setId("123456");
        List<Credentials> credentials = ImmutableList.of(credential);

        Provider provider = new Provider();
        provider.setDisplayName("Danske Bank");
        Map<String, Provider> providerByName = ImmutableMap.of("danskebank", provider);

        return new ProviderDisplayNameFinder(providerByName, credentials);
    }

    public static ProviderImageMap createProviderImageMap() {
        return new ProviderImageMap(
                ImmutableList.<ProviderImage>builder()
                        .add(createProviderImage("icon:danskebank", "url:icon:danskebank"))
                        .add(createProviderImage("banner:danskebank", "url:banner:danskebank"))
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
