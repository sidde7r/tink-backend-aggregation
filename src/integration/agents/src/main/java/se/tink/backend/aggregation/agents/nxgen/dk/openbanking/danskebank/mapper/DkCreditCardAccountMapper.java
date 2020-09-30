package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;

public class DkCreditCardAccountMapper extends CreditCardAccountMapper {

    private final DanskeDkIdentifierMapper danskeDkIdentifierMapper;

    public DkCreditCardAccountMapper(
            CreditCardBalanceMapper balanceMapper,
            DanskeDkIdentifierMapper danskeDkIdentifierMapper) {
        super(balanceMapper, danskeDkIdentifierMapper);
        this.danskeDkIdentifierMapper = danskeDkIdentifierMapper;
    }

    @Override
    public Optional<CreditCardAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties) {

        AccountIdentifierEntity cardIdentifier =
                danskeDkIdentifierMapper.getCreditCardIdentifier(account.getIdentifiers());
        String displayName = pickDisplayName(account, cardIdentifier);

        String uniqueIdentifier =
                danskeDkIdentifierMapper.formatIdentificationNumber(cardIdentifier);

        CreditCardBuildStep builder =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardIdentifier.getIdentification())
                                        .withBalance(balanceMapper.getAccountBalance(balances))
                                        .withAvailableCredit(
                                                balanceMapper.getAvailableCredit(balances))
                                        .withCardAlias(displayName)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(uniqueIdentifier)
                                        .withAccountNumber(cardIdentifier.getIdentification())
                                        .withAccountName(displayName)
                                        .addIdentifier(
                                                danskeDkIdentifierMapper.mapIdentifier(
                                                        cardIdentifier))
                                        .build())
                        .setApiIdentifier(account.getAccountId());

        collectHolders(cardIdentifier, parties).forEach(builder::addHolderName);
        return Optional.of(builder.build());
    }
}
