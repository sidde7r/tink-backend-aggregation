package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JyskeCardType {

    private String type;

    //    Possible other fields:
    //    private String name;
    //    private String infoText;

    public boolean isCreditCard() {
        return Optional.ofNullable(type)
                .map(String::toUpperCase)
                .filter(JyskeCardType::isNotADebitCard)
                .filter(JyskeCardType::isNotADanKort)
                .isPresent();
    }

    private static boolean isNotADanKort(String type) {
        return isNot(type, JyskeConstants.Fetcher.CreditCard.DANKORT);
    }

    private static boolean isNotADebitCard(String type) {
        return isNot(type, JyskeConstants.Fetcher.CreditCard.DEBIT);
    }

    private static boolean isNot(String type, String t) {
        return !type.contains(t);
    }
}
