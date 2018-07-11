package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListCreditCardsResponse extends ArrayList<SdcCreditCardEntity> {

    @JsonIgnore
    public List<SdcCreditCardEntity> getCreditCards() {
        Map<String, SdcCreditCardEntity> cardsByAccount = new HashMap<>();

        List<SdcCreditCardEntity>  creditCards = stream()
                .filter(SdcCreditCardEntity::isCreditCard)
                .collect(Collectors.toList());

        // filter any extra cards for an account, only display the most recent one
        creditCards.forEach(newCard -> {
            String creditCardAccountId = newCard.getAttachedAccount().getId();
            if (cardsByAccount.containsKey(creditCardAccountId)) {
                SdcCreditCardEntity addedCard = cardsByAccount.get(creditCardAccountId);

                if (isMoreRecent(addedCard, newCard)) {
                    cardsByAccount.put(creditCardAccountId, newCard);
                }
            } else {
                cardsByAccount.put(creditCardAccountId, newCard);
            }
        });

        return new ArrayList<>(cardsByAccount.values());
    }

    private boolean isMoreRecent(SdcCreditCardEntity addedCard, SdcCreditCardEntity newCard) {
        // if new card has end date and end date is after already added cards end date -> true
        if (newCard.getEndDate() != null) {
            if (addedCard.getEndDate() == null || addedCard.getEndDate().before(newCard.getEndDate())) {
                return true;
            }
        }

        return false;
    }
}
