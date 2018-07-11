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

        List<SdcCreditCardEntity>  creditCards = stream()
                .filter(SdcCreditCardEntity::isCreditCard)
                .collect(Collectors.toList());

        // filter any extra cards for an account, keep the most recent one
        Map<String, SdcCreditCardEntity> filteredCardAccounts = filterMostRecentCardPerAccount(creditCards);

        return new ArrayList<>(filteredCardAccounts.values());
    }

    // only save one credit card per account, save the most recent card
    private Map<String, SdcCreditCardEntity> filterMostRecentCardPerAccount(List<SdcCreditCardEntity> creditCards) {
        Map<String, SdcCreditCardEntity> cardsByAccount = new HashMap<>();

        for (SdcCreditCardEntity creditCard : creditCards) {
            String creditCardAccountId = creditCard.getAttachedAccount().getId();

            if (cardsByAccount.containsKey(creditCardAccountId)) {
                SdcCreditCardEntity addedCard = cardsByAccount.get(creditCardAccountId);

                if (isMoreRecent(addedCard, creditCard)) {
                    cardsByAccount.put(creditCardAccountId, creditCard);
                }
            } else {
                cardsByAccount.put(creditCardAccountId, creditCard);
            }
        }

        return cardsByAccount;
    }

    // if new card has end date and end date is after already added cards end date -> true
    private boolean isMoreRecent(SdcCreditCardEntity addedCard, SdcCreditCardEntity newCard) {
        if (newCard.getEndDate() == null) {
            return false;
        }
        if (addedCard.getEndDate() == null || addedCard.getEndDate().before(newCard.getEndDate())) {
            return true;
        }

        return false;
    }
}
