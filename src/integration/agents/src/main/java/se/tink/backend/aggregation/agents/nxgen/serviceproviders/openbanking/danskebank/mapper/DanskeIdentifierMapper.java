package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public abstract class DanskeIdentifierMapper extends DefaultIdentifierMapper {

    public DanskeIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        super(valueExtractor);
    }

    public String formatIdentificationNumber(AccountIdentifierEntity accountIdentifierEntity) {
        switch (accountIdentifierEntity.getIdentifierType()) {
            case BBAN:
                return formatBban(accountIdentifierEntity);
            case IBAN:
                return formatIban(accountIdentifierEntity);
            default:
                return formatAccountNumberIfIdentifierIsNotBbanOrIban(
                        accountIdentifierEntity.getIdentification());
        }
    }

    protected abstract String formatBban(AccountIdentifierEntity accountIdentifierEntity);

    private String formatIban(AccountIdentifierEntity accountIdentifierEntity) {
        String identification = accountIdentifierEntity.getIdentification();
        Matcher matcher = getMarketSpecificIdentifierPattern().matcher(identification);
        if (matcher.find()) {
            return formatAccountNumber(matcher.group(1));
        }
        return formatAccountNumberIfIbanDoesNotMatchThePattern(identification);
    }

    protected abstract Pattern getMarketSpecificIdentifierPattern();

    protected abstract String formatAccountNumber(String accountNumber);

    private String formatAccountNumberIfIdentifierIsNotBbanOrIban(String identification) {
        log.warn(
                "Found not matching account identifier to Danske Bank IBAN or BBAN. It begins with [{}]",
                getAccountNumberFirstCharacters(identification));
        return formatAccountNumber(StringUtils.right(identification, getAccountNoMinLength()));
    }

    protected abstract int getAccountNoMinLength();

    private String formatAccountNumberIfIbanDoesNotMatchThePattern(String identification) {
        log.warn(
                "Found IBAN not matching to the pattern. It begins with [{}] and has length [{}]",
                getAccountNumberFirstCharacters(identification),
                identification.length());
        return formatAccountNumber(
                StringUtils.substring(identification, getCharsToSubstringFromIban()));
    }

    protected abstract int getCharsToSubstringFromIban();

    private String getAccountNumberFirstCharacters(String identification) {
        return StringUtils.left(identification, 9);
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return getTransactionalAccountPrimaryIdentifier(
                identifiers, DanskebankV31Constant.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);
    }
}
