package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class SamlinkCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final SamlinkApiClient apiClient;

    public SamlinkCardFetcher(final SamlinkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return map(apiClient.fetchCardAccounts());
    }

    private Collection<CreditCardAccount> map(CardsResponse cardsResponse) {
        return Optional.ofNullable(cardsResponse.getCardAccounts()).orElse(Collections.emptyList())
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private CreditCardAccount map(CardEntity cardEntity) {
        String identifier =
                cardEntity.getName() != null ? cardEntity.getName() : cardEntity.getProduct();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(cardEntity.getMaskedPan())
                                .withBalance(cardEntity.getInterimBooked())
                                .withAvailableCredit(cardEntity.getInterimAvailable())
                                .withCardAlias(identifier)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardEntity.getResourceId())
                                .withAccountNumber(cardEntity.getMaskedPan())
                                .withAccountName(identifier)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                cardEntity.getMaskedPan()))
                                .build())
                .setApiIdentifier(cardEntity.getResourceId())
                .build();
    }
}
