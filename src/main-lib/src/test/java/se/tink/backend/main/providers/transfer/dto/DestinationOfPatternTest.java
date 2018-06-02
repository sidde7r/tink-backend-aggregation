package se.tink.backend.main.providers.transfer.dto;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.ProviderImage;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderImageMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DestinationOfPatternTest {
    @Test
    public void testModel() {
        List<AccountIdentifier> accountIdentifier = createAccountIdentifiers();
        UUID randomUUID = UUID.randomUUID();

        DestinationOfPattern destinationOfPattern = new DestinationOfPattern(accountIdentifier.get(0));
        destinationOfPattern.setBank("TestBank");
        destinationOfPattern.setName("MyName");
        destinationOfPattern.setPatternAccountId(randomUUID);

        Destination destination = destinationOfPattern;

        assertThat(destination.getName().get()).isEqualTo("MyName");
        assertThat(destination.getType()).isEqualTo(AccountTypes.EXTERNAL);
        assertThat(destination.getIdentifiers().size()).isEqualTo(1);
        assertThat(destination.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.SE);
        assertThat(destination.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("1200112233");
        assertThat(destination.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(destination.is(DestinationOfPattern.class)).isTrue();
        assertThat(destination.to(DestinationOfPattern.class).getBank()).isEqualTo("TestBank");
        assertThat(destination.to(DestinationOfPattern.class).getPatternAccountId()).isEqualTo(randomUUID);
    }

    @Test
    public void testShouldFormatNameReadable() {
        List<AccountIdentifier> accountIdentifier = createAccountIdentifiers();

        Destination destination = new DestinationOfPattern(accountIdentifier.get(0));
        destination.setName("GET ME READABLE NAME");

        assertThat(destination.getName().get()).isEqualTo("Get Me Readable Name");
    }

    @Test
    public void testCopy() {
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();
        UUID randomUUID = UUID.randomUUID();

        DestinationOfPattern destinationOfPattern = new DestinationOfPattern(accountIdentifiers.get(0));
        destinationOfPattern.setBank("TestBank");
        destinationOfPattern.setName("MyName");
        destinationOfPattern.setPatternAccountId(randomUUID);

        Destination destination = destinationOfPattern;
        Destination copyOf = destination.copyOf();

        assertThat(copyOf).isNotSameAs(destination);
        assertThat(copyOf.getIdentifiers()).isNotSameAs(destination);

        assertThat(copyOf.getName().get()).isEqualTo("MyName");
        assertThat(copyOf.getType()).isEqualTo(AccountTypes.EXTERNAL);
        assertThat(copyOf.getIdentifiers().size()).isEqualTo(1);
        assertThat(copyOf.getPrimaryIdentifier().get().getType()).isEqualTo(AccountIdentifier.Type.SE);
        assertThat(copyOf.getPrimaryIdentifier().get().getIdentifier()).isEqualTo("1200112233");
        assertThat(copyOf.getDisplayIdentifier().get().getIdentifier(new DisplayAccountIdentifierFormatter())).isEqualTo("1200-112233");

        assertThat(copyOf.is(DestinationOfPattern.class)).isTrue();
        assertThat(destination.to(DestinationOfPattern.class).getBank()).isEqualTo("TestBank");
        assertThat(destination.to(DestinationOfPattern.class).getPatternAccountId()).isEqualTo(randomUUID);
    }

    @Test
    public void testImageUrls() {
        UUID randomUUID = UUID.randomUUID();
        List<AccountIdentifier> accountIdentifiers = createAccountIdentifiers();
        ProviderImageMap providerImageMap = createProviderImageMap();

        DestinationOfPattern destinationOfPattern = new DestinationOfPattern(accountIdentifiers.get(0));
        destinationOfPattern.setBank("Danske Bank");
        destinationOfPattern.setName("MyName");
        destinationOfPattern.setPatternAccountId(randomUUID);

        ImageUrls imageUrls = destinationOfPattern
                .getImageUrls(providerImageMap);

        assertThat(imageUrls.getBanner()).isEqualTo("url:banner:danskebank");
        assertThat(imageUrls.getIcon()).isEqualTo("url:icon:danskebank");
    }

    private static List<AccountIdentifier> createAccountIdentifiers() {
        AccountIdentifier swedishIdentifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");

        return ImmutableList.of(swedishIdentifier);
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
