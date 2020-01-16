package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.partner;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NordeaPartnerSeAccountMapper implements NordeaPartnerAccountMapper {

    @Override
    public Optional<TransactionalAccount> toTinkTransactionalAccount(AccountEntity account) {
        final SwedishIdentifier seIdentifier = getAccountIdentifier(account);
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaPartnerConstants.TRANSACTIONAL_ACCOUNT_TYPE_MAPPER,
                        account.getCategory())
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        account.getAvailableBalance(), account.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(account.getIban())
                                .withAccountNumber(seIdentifier.getIdentifier())
                                .withAccountName(account.getNickname())
                                .addIdentifier(seIdentifier)
                                .addIdentifier(
                                        AccountIdentifier.create(Type.IBAN, account.getIban()))
                                .build())
                .addHolderName(account.getHolderName())
                .setApiIdentifier(account.getAccountId())
                .build();
    }

    private SwedishIdentifier getAccountIdentifier(AccountEntity account) {
        final String formattedAccountNumber = formatAccountNumber(account.getAccountId());
        if (formattedAccountNumber.length() == NDAPersonalNumberIdentifier.LENGTH) {
            final AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(Type.SE_NDA_SSN, formattedAccountNumber);
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
            }
        }
        return new SwedishIdentifier(formattedAccountNumber);
    }

    public String formatAccountNumber(String accountNumber) {
        final Pattern p = Pattern.compile(".*?([0-9]{10,})");
        // account number comes in format "NAID-SE-SEK-<ACCOUNTNUMBER>"
        final Matcher matcher = p.matcher(accountNumber);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Could not parse account number from " + accountNumber);
        }
        return matcher.group(1);
    }
}
