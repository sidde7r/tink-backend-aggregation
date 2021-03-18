package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoBpiCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private BancoBpiClientApi clientApi;

    public BancoBpiCreditCardAccountFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            List<BancoBpiProductData> cards =
                    clientApi.getProductsByType(BancoBpiProductType.CREDIT_CARD);
            return mapToTinkModel(cards);
        } catch (RequestException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private List<CreditCardAccount> mapToTinkModel(List<BancoBpiProductData> cards) {
        List<CreditCardAccount> creditCardAccounts = new ArrayList<>(cards.size());
        for (BancoBpiProductData card : cards) {
            creditCardAccounts.add(
                    CreditCardAccount.nxBuilder()
                            .withCardDetails(
                                    CreditCardModule.builder()
                                            .withCardNumber(card.getNumber())
                                            .withBalance(
                                                    ExactCurrencyAmount.of(
                                                            card.getBalance(),
                                                            card.getCurrencyCode()))
                                            .withAvailableCredit(
                                                    ExactCurrencyAmount.of(
                                                            card.getInitialBalance(),
                                                            card.getCurrencyCode()))
                                            .withCardAlias(card.getName())
                                            .build())
                            .withInferredAccountFlags()
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(card.getNumber())
                                            .withAccountNumber(card.getNumber())
                                            .withAccountName(card.getName())
                                            .addIdentifier(
                                                    AccountIdentifier.create(
                                                            AccountIdentifierType
                                                                    .PAYMENT_CARD_NUMBER,
                                                            card.getNumber()))
                                            .setProductName(card.getName())
                                            .build())
                            .addHolderName(card.getOwner())
                            .setApiIdentifier(card.getNumber())
                            .build());
        }
        return creditCardAccounts;
    }
}
