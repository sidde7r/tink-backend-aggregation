package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryDataEntity extends StatusEntity {
    private UserDataEntity userData;
    private List<CardDetailsEntity> cardList;

    public UserDataEntity getUserData() {
        return userData;
    }

    public void setUserData(UserDataEntity userData) {
        this.userData = userData;
    }

    public List<CardDetailsEntity> getCardList() {

        if (cardList == null) {
            return Collections.emptyList();
        }

        return cardList.stream()
                .filter(
                        cardDetail -> {
                            if (cardDetail.getMessage() != null
                                    && cardDetail.getMessage().getShortValue() != null) {
                                return !"cancelled"
                                        .equalsIgnoreCase(cardDetail.getMessage().getShortValue());
                            }
                            return true;
                        })
                .collect(Collectors.toList());
    }

    public void setCardList(List<CardDetailsEntity> cardList) {
        this.cardList = cardList;
    }
}
