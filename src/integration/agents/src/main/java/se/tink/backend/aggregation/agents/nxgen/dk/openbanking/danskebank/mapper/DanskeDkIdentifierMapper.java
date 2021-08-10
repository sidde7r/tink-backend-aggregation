package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeDkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeDkNoIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public class DanskeDkIdentifierMapper extends DanskeDkNoIdentifierMapper {

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
    public Collection<AccountIdentifier> mapIdentifiers(
            List<AccountIdentifierEntity> accountIdentifiers) {
        return accountIdentifiers.stream()
                .filter(
                        accountIdentifierEntity ->
                                accountIdentifierEntity.getIdentifierType() == IBAN)
                .findFirst()
                .map(
                        ibanIdentifier ->
                                Arrays.asList(
                                        new IbanIdentifier(ibanIdentifier.getIdentification()),
                                        getBbanFromIban(ibanIdentifier)))
                .map(Collections::unmodifiableCollection)
                .orElseGet(() -> super.mapIdentifiers(accountIdentifiers));
    }

    private BbanIdentifier getBbanFromIban(AccountIdentifierEntity accountIdentifierEntity) {
        return new BbanIdentifier(accountIdentifierEntity.getIdentification().substring(4));
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
