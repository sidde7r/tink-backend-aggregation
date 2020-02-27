package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

@RequiredArgsConstructor
public class CreditCardAccountMapper {

    private final CreditCardBalanceMapper balanceMapper;

    public CreditCardAccount map(
            AccountEntity account, Collection<AccountBalanceEntity> balances, String partyName) {

        AccountIdentifierEntity cardIdentifier = extractCardIdentifier(account);
        String displayName = pickDisplayName(account, cardIdentifier);

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(cardIdentifier.getIdentification())
                                .withBalance(
                                        balanceMapper
                                                .getAccountBalance(balances)
                                                .getAsCurrencyAmount())
                                .withAvailableCredit(balanceMapper.getAvailableCredit(balances))
                                .withCardAlias(displayName)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardIdentifier.getIdentification())
                                .withAccountNumber(cardIdentifier.getIdentification())
                                .withAccountName(displayName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER,
                                                cardIdentifier.getIdentification()))
                                .build())
                .addHolderName(ObjectUtils.firstNonNull(cardIdentifier.getOwnerName(), partyName))
                .setApiIdentifier(account.getAccountId())
                .build();
    }

    private AccountIdentifierEntity extractCardIdentifier(AccountEntity account) {
        return account.getIdentifiers().stream()
                .filter(i -> i.getIdentifierType().equals(ExternalAccountIdentification4Code.PAN))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing PAN card identifier"));
    }

    private String pickDisplayName(
            AccountEntity account, AccountIdentifierEntity cardPrimaryAccountNumber) {
        return ObjectUtils.firstNonNull(
                account.getNickname(),
                cardPrimaryAccountNumber.getOwnerName(),
                cardPrimaryAccountNumber.getIdentification());
    }
}
