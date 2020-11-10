package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.entity.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;

@AllArgsConstructor
public class CreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private FetcherClient fetcherClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return fetcherClient.fetchCreditCards().getCards().stream()
                .filter(CreditCardEntity::isCreditCard)
                .map(card -> fetcherClient.fetchCreditCardDetails(card.getCardId()))
                .map(this::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount toTinkCreditCardAccount(CreditCardDetailsResponse cardDetails) {
        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(cardDetails.getMaskedCreditCardNumber())
                        .withBalance(cardDetails.getBookedBalance())
                        .withAvailableCredit(cardDetails.getAvailableBalance())
                        .withCardAlias(
                                ObjectUtils.firstNonNull(
                                        cardDetails.getNickname(),
                                        cardDetails.getMaskedCreditCardNumber()))
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(cardDetails.getMaskedCreditCardNumber())
                        .withAccountNumber(cardDetails.getMaskedCreditCardNumber())
                        .withAccountName(
                                ObjectUtils.firstNonNull(
                                        cardDetails.getNickname(),
                                        cardDetails.getMaskedCreditCardNumber()))
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.PAYMENT_CARD_NUMBER,
                                        cardDetails.getMaskedCreditCardNumber()))
                        .build();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardModule)
                .withInferredAccountFlags()
                .withId(idModule)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .setApiIdentifier(cardDetails.getCardId())
                .build();
    }
}
