package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard;

import com.google.common.base.Preconditions;
import java.util.Optional;

public class KeyCardInitValues {
    private final String cardId;
    private final String cardIndex;

    public KeyCardInitValues(String cardId, String cardIndex) {
        this.cardId = Preconditions.checkNotNull(cardId, "Card Id must be set");
        this.cardIndex = Preconditions.checkNotNull(cardIndex, "Card index must be set");
    }

    public static KeyCardInitValues createFromCardIdAndCardIndex(String cardId, String cardIndex) {
        return new KeyCardInitValues(cardId, cardIndex);
    }

    public KeyCardInitValues(String cardIndex) {
        this.cardId = null;
        this.cardIndex = Preconditions.checkNotNull(cardIndex, "Card index must be set");
    }

    public Optional<String> getCardId() {
        return Optional.ofNullable(cardId);
    }

    public String getCardIndex() {
        return cardIndex;
    }
}
