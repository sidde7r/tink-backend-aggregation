package se.tink.backend.main.providers.transfer.dto;

import com.google.common.base.Function;
import java.util.Optional;
import com.google.common.base.Predicate;
import java.util.UUID;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.backend.utils.StringUtils;

public class DestinationOfPattern extends Destination {
    public static final Function<TransferDestinationPattern, Destination> TRANSFERDESTINATIONPATTERN_TO_DESTINATION =
            transferDestinationPattern -> {
                AccountIdentifier accountIdentifier = transferDestinationPattern.getAccountIdentifier().get();

                DestinationOfPattern destination = new DestinationOfPattern(accountIdentifier);

                destination.setBank(transferDestinationPattern.getBank());
                destination.setName(transferDestinationPattern.getName());
                destination.setPatternAccountId(transferDestinationPattern.getAccountId());

                return destination;
            };

    public static final Predicate<TransferDestinationPattern> TRANSFERDESTINATIONPATTERN_IS_EXACT_ACCOUNTNUMBER =
            transferDestinationPattern -> !transferDestinationPattern.isMatchesMultiple();

    private String bank;
    private UUID patternAccountId;

    public DestinationOfPattern(AccountIdentifier identifier) {
        super(identifier);
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    @Override
    public void setName(String name) {
        super.setName(StringUtils.formatHuman(name));
    }

    public UUID getPatternAccountId() {
        return patternAccountId;
    }

    public void setPatternAccountId(UUID patternAccountId) {
        this.patternAccountId = patternAccountId;
    }

    public AccountTypes getType() {
        return AccountTypes.EXTERNAL;
    }

    @Override
    public Destination copyOf() {
        DestinationOfPattern destination = new DestinationOfPattern(this.getFirstIdentifier().orElse(null));

        destination.setName(this.getName().orElse(null));

        destination.setBank(this.bank);
        destination.setPatternAccountId(this.patternAccountId);

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

    public ImageUrls getImageUrls(ProviderImageMap providerImages) {
        return providerImages.getImagesForAccountIdentifier(getDisplayIdentifier().orElse(null));
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        DestinationOfPattern that = (DestinationOfPattern) o;

        if (bank != null ? !bank.equals(that.bank) : that.bank != null) {
            return false;
        }
        return patternAccountId != null ?
                patternAccountId.equals(that.patternAccountId) :
                that.patternAccountId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + (patternAccountId != null ? patternAccountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toStringHelper(this.getClass())
                .add("bank", bank)
                .add("patternAccountId", patternAccountId)
                .toString();
    }
}
