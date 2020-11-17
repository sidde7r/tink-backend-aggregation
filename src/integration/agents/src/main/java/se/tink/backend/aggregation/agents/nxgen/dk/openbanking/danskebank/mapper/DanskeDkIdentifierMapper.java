package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeDkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public class DanskeDkIdentifierMapper extends DanskeIdentifierMapper {

    public DanskeDkIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        super(valueExtractor);
    }

    @Override
    protected String formatBban(AccountIdentifierEntity accountIdentifierEntity) {
        return getAccountNumberWithZerosIfIsTooShort(
                accountIdentifierEntity
                        .getIdentification()
                        .substring(DanskeDkConstants.BRANCH_CODE_LENGTH));
    }

    @Override
    protected Pattern getMarketSpecificIdentifierPattern() {
        return DanskeDkConstants.EXTRACT_ACCOUNT_NO_FROM_IBAN_PATTERN;
    }

    @Override
    protected int getAccountNoMinLength() {
        return DanskeDkConstants.ACCOUNT_NO_MIN_LENGTH;
    }

    @Override
    protected int getCharsToSubstringFromIban() {
        return DanskeDkConstants.CHARS_TO_SUBSTRING_FROM_IBAN;
    }

    @Override
    protected String formatAccountNumber(String accountNumber) {
        return getAccountNumberWithZerosIfIsTooShort(accountNumber);
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        if (accountNumber.length() < getAccountNoMinLength()) {
            log.warn(
                    "Found accountNumber shorter than expected. It has length [{}]",
                    accountNumber.length());
        }
        return Strings.padStart(accountNumber, getAccountNoMinLength(), '0');
    }
}
