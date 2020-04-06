package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

@RunWith(JUnitParamsRunner.class)
public class DefaultAccountTypeMapperTest {

    private Object[] supportedTypesParameters() {
        return new Object[] {
            new Object[] {1, AccountTypes.CHECKING},
            new Object[] {5, AccountTypes.CHECKING},
            new Object[] {9, AccountTypes.CHECKING},
            new Object[] {10, AccountTypes.SAVINGS},
            new Object[] {15, AccountTypes.SAVINGS},
            new Object[] {19, AccountTypes.SAVINGS},
            new Object[] {30, AccountTypes.INVESTMENT},
            new Object[] {35, AccountTypes.INVESTMENT},
            new Object[] {39, AccountTypes.INVESTMENT},
            new Object[] {50, AccountTypes.CREDIT_CARD},
            new Object[] {59, AccountTypes.CREDIT_CARD},
            new Object[] {60, AccountTypes.INVESTMENT},
            new Object[] {65, AccountTypes.INVESTMENT},
            new Object[] {69, AccountTypes.INVESTMENT}
        };
    }

    @Test
    @Parameters(method = "supportedTypesParameters")
    public void shouldCategorizeIntoSupportedAccountTypesProperly(
            int finTsAccountType, AccountTypes expectedTinkAccountType) {
        // given
        AccountTypeMapper mapper = new DefaultAccountTypeMapper();
        HIUPD hiupd = new HIUPD().setAccountType(finTsAccountType);

        // when
        Optional<AccountTypes> maybeTinkAccountType = mapper.getAccountTypeFor(hiupd);

        // then
        assertThat(maybeTinkAccountType.isPresent()).isTrue();
        assertThat(maybeTinkAccountType.get()).isEqualTo(expectedTinkAccountType);
    }

    @Test
    @Parameters({"20", "22", "40", "49", "70", "82", "99"})
    public void shouldReturnEmptyOptionalIfNoMappingFound(int finTsAccountType) {
        // given
        AccountTypeMapper mapper = new DefaultAccountTypeMapper();
        HIUPD hiupd = new HIUPD().setAccountType(finTsAccountType);

        // when
        Optional<AccountTypes> maybeTinkAccountType = mapper.getAccountTypeFor(hiupd);

        // then
        assertThat(maybeTinkAccountType.isPresent()).isFalse();
    }
}
