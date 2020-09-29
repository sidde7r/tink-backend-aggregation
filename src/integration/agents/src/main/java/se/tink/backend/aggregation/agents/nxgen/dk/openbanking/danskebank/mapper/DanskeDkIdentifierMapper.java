package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Collection;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public class DanskeDkIdentifierMapper extends IdentifierMapper {

    public DanskeDkIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        super(valueExtractor);
    }

    String formatIdentificationNumber(AccountIdentifierEntity accountIdentifierEntity) {
        if (BBAN.equals(accountIdentifierEntity.getIdentifierType())) {
            return formatBbanIntoAccountNumber(accountIdentifierEntity);
        } else if (IBAN.equals(accountIdentifierEntity.getIdentifierType())) {
            return formatAccountNumberIfIdentifierIsIban(accountIdentifierEntity);
        }
        return formatAccountNumberIfIdentifierIsNotBbanOrIban(
                accountIdentifierEntity.getIdentification());
    }

    private String formatBbanIntoAccountNumber(AccountIdentifierEntity accountIdentifierEntity) {
        return getAccountNumberWithZerosIfIsTooShort(
                accountIdentifierEntity
                        .getIdentification()
                        .substring(DanskeConstants.BRANCH_CODE_LENGTH));
    }

    private String formatAccountNumberIfIdentifierIsIban(
            AccountIdentifierEntity accountIdentifierEntity) {
        String identification = accountIdentifierEntity.getIdentification();
        Matcher matcher =
                DanskeConstants.EXTRACT_ACCOUNT_NO_FROM_IBAN_PATTERN.matcher(identification);
        if (matcher.find()) {
            return getAccountNumberWithZerosIfIsTooShort(matcher.group(1));
        }
        return formatAccountNumberIfIdentifierIsNotBbanOrIban(identification);
    }

    private String formatAccountNumberIfIdentifierIsNotBbanOrIban(String identification) {
        log.warn(
                "Found not matching account identifier to Danske Bank IBAN. It begins with [{}]",
                getAccountFirstCharacters(identification));
        return getAccountNumberWithZerosIfIsTooShort(StringUtils.substring(identification, 8));
    }

    private String getAccountFirstCharacters(String identification) {
        return StringUtils.left(identification, 9);
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return getTransactionalAccountPrimaryIdentifier(
                identifiers, DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        return Strings.padStart(accountNumber, DanskeConstants.ACCOUNT_NO_MIN_LENGTH, '0');
    }
}
