package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RevolutAccountIdentifierFilterTest {
    private static final String ACCOUNT_WITH_US_ROUTING_NUMBER =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Business\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"},{\"SchemeName\":\"US.RoutingNumberAccountNumber\",\"Identification\":\"1234 1111 5678 1234 55 6666\",\"Name\":\"Myszo Jelen\"}]}";
    private static final String ACCOUNT_WITH_DUPLICATE_IBAN_IDENTIFIER =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Business\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";

    @Test
    public void shouldIgnoreNotSupportedAccount() {
        // given
        AccountEntity account =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_WITH_US_ROUTING_NUMBER, AccountEntity.class);
        // when
        List<AccountIdentifierEntity> identifiers =
                RevolutAccountIdentifierFilter.getFilteredAccountIdentifiers(account);

        // then
        assertTrue(identifiers.size() == 1);
    }

    @Test
    public void shouldIgnoreDuplicateIbanIdentifiers() {
        // given
        AccountEntity account =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_WITH_DUPLICATE_IBAN_IDENTIFIER, AccountEntity.class);
        // when
        List<AccountIdentifierEntity> identifiers =
                RevolutAccountIdentifierFilter.getFilteredAccountIdentifiers(account);

        // then
        assertTrue(identifiers.size() == 1);
    }
}
