package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;

@RequiredArgsConstructor
public class CreditCardAccountMapper {

    private final CreditCardBalanceMapper balanceMapper;
    private final IdentifierMapper identifierMapper;

    public CreditCardAccount map(
            AccountEntity account, Collection<AccountBalanceEntity> balances, String partyName) {

        AccountIdentifierEntity cardIdentifier =
                identifierMapper.getCreditCardIdentifier(account.getIdentifiers());
        String displayName = pickDisplayName(account, cardIdentifier);

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(cardIdentifier.getIdentification())
                                .withBalance(balanceMapper.getAccountBalance(balances))
                                .withAvailableCredit(balanceMapper.getAvailableCredit(balances))
                                .withCardAlias(displayName)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardIdentifier.getIdentification())
                                .withAccountNumber(cardIdentifier.getIdentification())
                                .withAccountName(displayName)
                                .addIdentifier(identifierMapper.mapIdentifier(cardIdentifier))
                                .build())
                .addHolderName(ObjectUtils.firstNonNull(cardIdentifier.getOwnerName(), partyName))
                .setApiIdentifier(account.getAccountId())
                .build();
    }

    private String pickDisplayName(
            AccountEntity account, AccountIdentifierEntity cardPrimaryAccountNumber) {
        return ObjectUtils.firstNonNull(
                account.getNickname(),
                cardPrimaryAccountNumber.getOwnerName(),
                cardPrimaryAccountNumber.getIdentification());
    }
}
