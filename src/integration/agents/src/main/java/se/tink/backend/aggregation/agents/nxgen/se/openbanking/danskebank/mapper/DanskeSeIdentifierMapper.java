package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank.mapper;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeSeIdentifierMapper extends DanskeIdentifierMapper {
    private static final String NOT_IMPLEMENTED = "Not Implemented";

    public DanskeSeIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        super(valueExtractor);
    }

    @Override
    protected String formatBban(AccountIdentifierEntity accountIdentifierEntity) {
        return accountIdentifierEntity.getIdentification();
    }

    @Override
    protected Pattern getMarketSpecificIdentifierPattern() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    protected String formatAccountNumber(String accountNumber) {
        return accountNumber;
    }

    @Override
    protected int getAccountNoMinLength() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    protected int getCharsToSubstringFromIban() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
