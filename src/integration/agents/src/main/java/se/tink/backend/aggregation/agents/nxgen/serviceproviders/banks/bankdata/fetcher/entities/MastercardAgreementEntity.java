package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class MastercardAgreementEntity {
    private String agreementId;
    private String agreementName;
    private Double balance;
    private Double availableBalance;
    private Double maxBalance;
    private String regNo;
    private String accountNo;
    private Boolean canDeposit;
    private List<MastercardEntity> mastercardCards;

    public String getAgreementId() {
        return agreementId;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public Double getMaxBalance() {
        return maxBalance;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public Boolean getCanDeposit() {
        return canDeposit;
    }

    public List<MastercardEntity> getMastercardCards() {
        return mastercardCards;
    }

    private String constructUniqueIdentifier() {
        String accountId = regNo + ":" + accountNo;
        Preconditions.checkState(StringUtils.trimToNull(accountId) != null, "No account number present");

        return accountId;
    }

    public Optional<CreditCardAccount> createCreditCardAccount() {
        // Get the card that belongs to the account owner, is active, and in the market's currency
        Optional<MastercardEntity> cardDetails = getAccountOwnerCardDetails();

        return cardDetails.map(mastercardEntity -> CreditCardAccount.builder(
                constructUniqueIdentifier(), Amount.inDKK(balance), Amount.inDKK(maxBalance))
                .setAccountNumber(mastercardEntity.getCardNo())
                .setName(mastercardEntity.getCardName())
                .setBankIdentifier(constructUniqueIdentifier())
                .build());
    }

    private Optional<MastercardEntity> getAccountOwnerCardDetails() {
        if (mastercardCards == null) {
            return Optional.empty();
        }

        return mastercardCards.stream()
                .filter(this::isValidCard)
                .findFirst();
    }

    private boolean isValidCard(MastercardEntity mastercardEntity) {
        String agreementAccountOwner = mastercardEntity.getAgreementAccountOwner();
        String cardUser = mastercardEntity.getCardUser();

        return !Objects.isNull(agreementAccountOwner) &&
                !Objects.isNull(cardUser) &&
                agreementAccountOwner.equalsIgnoreCase(cardUser) &&
                !mastercardEntity.getStopped() &&
                isMarketCurrency(mastercardEntity.getBalanceCurrency());
    }

    private boolean isMarketCurrency(String currency) {
        return BankdataConstants.MARKET_CURRENCY.equalsIgnoreCase(currency);
    }
}
