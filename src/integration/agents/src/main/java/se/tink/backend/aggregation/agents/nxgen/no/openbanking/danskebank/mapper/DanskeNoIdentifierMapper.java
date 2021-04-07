package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.mapper;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.DanskeNoConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeDkNoIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeNoIdentifierMapper extends DanskeDkNoIdentifierMapper {

    public DanskeNoIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        super(valueExtractor);
    }

    @Override
    protected String formatBban(AccountIdentifierEntity accountIdentifierEntity) {
        return accountIdentifierEntity.getIdentification();
    }

    @Override
    protected Pattern getMarketSpecificIdentifierPattern() {
        return DanskeNoConstants.EXTRACT_BBAN_FROM_IBAN_PATTERN;
    }

    @Override
    protected int getAccountNoMinLength() {
        return DanskeNoConstants.ACCOUNT_NO_MIN_LENGTH;
    }

    @Override
    protected int getCharsToSubstringFromIban() {
        return DanskeNoConstants.CHARS_TO_SUBSTRING_FROM_IBAN;
    }

    @Override
    protected String formatAccountNumber(String accountNumber) {
        return accountNumber;
    }
}
