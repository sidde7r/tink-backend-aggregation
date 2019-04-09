package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.registerCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.BaseResponse;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.requests.RegisterCardRequest;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;
import se.tink.backend.aggregation.log.AggregationLogger;

public class RegisteredCards extends BaseResponse {
    private static final AggregationLogger log = new AggregationLogger(RegisteredCards.class);

    @JsonProperty("_ProductCode")
    private List<String> cardTypes;

    public List<String> getCardTypes() {
        return cardTypes;
    }

    public void setCardTypes(List<RegisterCardRequest> cards) {
        if (cards != null && cards.size() > 0) {
            cardTypes = Lists.newArrayList();

            for (RegisterCardRequest card : cards) {
                String cardType = card.getCardType();

                if (!Strings.isNullOrEmpty(cardType)) {
                    cardTypes.add(cardType);
                }
            }
        }
    }

    public void logRequestedAndRegisteredCardTypes(
            Credentials credentials, String requestedCardType) {
        if (cardTypes != null && cardTypes.size() > 0) {
            log.info(
                    MoreObjects.toStringHelper(Card.class)
                            .add("requestCardType", requestedCardType)
                            .add("responseCardTypes", Joiner.on(", ").join(cardTypes))
                            .toString());
        } else {
            log.info("Failed to register card {requestedCardType=" + requestedCardType + "}");
        }
    }
}
