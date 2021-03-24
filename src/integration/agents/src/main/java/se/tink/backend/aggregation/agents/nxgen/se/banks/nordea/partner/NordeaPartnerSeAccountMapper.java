package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.partner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.DefaultPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class NordeaPartnerSeAccountMapper extends DefaultPartnerAccountMapper {
    @Override
    protected List<AccountIdentifier> getAccountIdentifiers(AccountEntity account) {
        final List<AccountIdentifier> identifiers = new ArrayList<>();
        identifiers.add(getSwedishIdentifier(account));
        identifiers.addAll(super.getAccountIdentifiers(account));
        return identifiers;
    }

    private SwedishIdentifier getSwedishIdentifier(AccountEntity account) {
        final String formattedAccountNumber = formatAccountNumber(account.getAccountId());
        if (formattedAccountNumber.length() == NDAPersonalNumberIdentifier.LENGTH) {
            final AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(
                            AccountIdentifierType.SE_NDA_SSN, formattedAccountNumber);
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
