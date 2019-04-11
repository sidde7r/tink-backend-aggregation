package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.utils.IkanoParser;

public class CardEntityTest {
    private static final String AVAILABLE_CREDIT = "3500";
    private static final String CARD_NAME = "Preem MasterCard";
    private static final String AGREEMENT_NUMBER = "982069793";

    @Test
    public void toTinkAccount() {
        CardEntity card = new CardEntity();
        card.setAvailableCredit(AVAILABLE_CREDIT);
        card.setAgreementNumber(AGREEMENT_NUMBER);
        card.setCardName(CARD_NAME);
        card.setCreditLimit("5000");

        Account account = card.toTinkAccount();

        assertThat(account.getBankId()).isEqualTo(AGREEMENT_NUMBER);
        assertThat(account.getName()).isEqualTo(CARD_NAME);
        assertThat(account.getBalance()).isEqualTo(-1500);
        assertThat(account.getAvailableCredit())
                .isEqualTo(IkanoParser.stringToDouble(AVAILABLE_CREDIT));
    }
}
