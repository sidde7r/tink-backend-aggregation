package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.registerCard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.BaseResponse;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.requests.RegisterCardRequest;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards.Card;

public class RegisteredCards extends BaseResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            logger.info(
                    MoreObjects.toStringHelper(Card.class)
                            .add("requestCardType", requestedCardType)
                            .add("responseCardTypes", Joiner.on(", ").join(cardTypes))
                            .toString());
        } else {
            logger.info("Failed to register card {requestedCardType=" + requestedCardType + "}");
        }
    }
}
