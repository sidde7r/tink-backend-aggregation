package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.utils.MarketParameters;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetailsEntity {
    private String homeCountryLocale;
    private String cardProductName;
    private int sortedIndex;
    private String cardKey;
    private String cardNumberDisplay;
    private String cardArtImage;
    private MessageEntity message;

    public MessageEntity getMessage() {
        return message;
    }

    private CardDetailsSummaryEntity summary;

    public String getHomeCountryLocale() {
        return homeCountryLocale;
    }

    public void setHomeCountryLocale(String homeCountryLocale) {
        this.homeCountryLocale = homeCountryLocale;
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public void setCardProductName(String cardProductName) {
        this.cardProductName = cardProductName;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public String getCardNumberDisplay() {
        return cardNumberDisplay;
    }

    public void setCardNumberDisplay(String cardNumberDisplay) {
        this.cardNumberDisplay = cardNumberDisplay;
    }

    public String getCardArtImage() {
        return cardArtImage;
    }

    public void setCardArtImage(String cardArtImage) {
        this.cardArtImage = cardArtImage;
    }

    public CardDetailsSummaryEntity getSummary() {
        return summary;
    }

    public void setSummary(CardDetailsSummaryEntity summary) {
        this.summary = summary;
    }

    public Account toAccount(MarketParameters marketParameters) {
        Account a = new Account();

        a.setName(cardProductName + " - " + cardNumberDisplay.substring(cardNumberDisplay.length() - 5));
        a.setAccountNumber(cardNumberDisplay);

        if (marketParameters.isUseOldBankId()) {
            a.setBankId(cardNumberDisplay.replaceAll("[^\\dA-Za-z]", ""));
        } else {
            a.setBankId(cardKey);
        }

        a.setType(AccountTypes.CREDIT_CARD);

        String totalBalance = summary.getTotalBalance().getValue();

        // Amex have twin cards that are that are connected to the same credit card account as another card.
        // The total balance of these cards is set to "n/a". We have not yet figured out how to display twin
        // and extra cards in the app, so for now we just set the total balance to 0 in the "n/a" case.
        if (Objects.equal(totalBalance.toLowerCase(), "n/a")) {
            a.setBalance(0);
        } else {
            a.setBalance(-StringUtils.parseAmount(totalBalance.replace("£", "").replace("$", "")
                    .replace("kr", "")));
        }
        return a;
    }
}
