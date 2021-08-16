package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public abstract class DanskeDkNoIdentifierMapper implements IdentifierMapper {

    private final PrioritizedValueExtractor valueExtractor;
    private final DefaultIdentifierMapper defaultMapper;

    public DanskeDkNoIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
        this.defaultMapper = new DefaultIdentifierMapper(valueExtractor);
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
                .orElseGet(() -> defaultMapper.mapIdentifiers(accountIdentifiers));
    }

    private BbanIdentifier getBbanFromIban(AccountIdentifierEntity accountIdentifierEntity) {
        return new BbanIdentifier(accountIdentifierEntity.getIdentification().substring(4));
    }

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        return defaultMapper.mapIdentifier(id);
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return valueExtractor
                .pickByValuePriority(
                        identifiers,
                        AccountIdentifierEntity::getIdentifierType,
                        DanskebankV31Constant.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account identifier. No available identifier with type of: "
                                                + StringUtils.join(
                                                        DanskebankV31Constant
                                                                .ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS,
                                                        ", ")));
    }

    public AccountIdentifierEntity getCreditCardAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return valueExtractor
                .pickByValuePriority(
                        identifiers,
                        AccountIdentifierEntity::getIdentifierType,
                        DanskebankV31Constant.ALLOWED_CREDIT_CARD_ACCOUNT_IDENTIFIERS)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account identifier. No available identifier with type of: "
                                                + StringUtils.join(
                                                        DanskebankV31Constant
                                                                .ALLOWED_CREDIT_CARD_ACCOUNT_IDENTIFIERS,
                                                        ", ")));
    }

    @Override
    public String getUniqueIdentifier(AccountIdentifierEntity accountIdentifier) {
        return formatIdentificationNumber(accountIdentifier);
    }

    private String formatIdentificationNumber(AccountIdentifierEntity accountIdentifierEntity) {
        switch (accountIdentifierEntity.getIdentifierType()) {
            case IBAN:
                return formatIban(accountIdentifierEntity);
            case BBAN:
                return formatBban(accountIdentifierEntity);
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
        return getCreditCardAccountPrimaryIdentifier(identifiers);
    }
}
