package se.tink.backend.main.providers.transfer.dto;

import com.google.common.base.Function;
import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.utils.ProviderImageMap;

public class DestinationOfExplicitDestination extends Destination {
    public static final Function<AccountIdentifier, DestinationOfExplicitDestination>
            EXPLICITDESTINATION_TO_DESTINATION =
            accountIdentifier -> {

                DestinationOfExplicitDestination destination =
                        new DestinationOfExplicitDestination(accountIdentifier);

                if (accountIdentifier.getName().isPresent()) {
                    destination.setName(accountIdentifier.getName().get());
                }

                return destination;
            };

    public DestinationOfExplicitDestination(AccountIdentifier identifier) {
        super(identifier);
    }

    public AccountTypes getType() {
        return AccountTypes.EXTERNAL;
    }

    public ImageUrls getImageUrls(ProviderImageMap providerImages) {

        Optional<AccountIdentifier> identifier = getPrimaryIdentifier();

        ImageUrls images;
        if (identifier.isPresent()) {
            images = providerImages.getImagesForAccountIdentifier(identifier.get());
        } else {
            images = new ImageUrls();
        }

        return images;
    }

    @Override
    public Destination copyOf() {
        DestinationOfExplicitDestination destination = new DestinationOfExplicitDestination(
                this.getFirstIdentifier().orElse(null));

        destination.setName(this.getName().orElse(null));

        return destination;
    }

    @Override
    public Optional<AccountIdentifier> getPrimaryIdentifier() {
        return getFirstIdentifier();
    }

    @Override
    public Optional<AccountIdentifier> getDisplayIdentifier() {
        return getFirstIdentifier();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toStringHelper(this.getClass()).toString();
    }
}
