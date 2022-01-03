package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.BalanceTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitionsTest.BalanceTypeMapperTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitionsTest.ConsentStatusTest;

@RunWith(Suite.class)
@SuiteClasses({ConsentStatusTest.class, BalanceTypeMapperTest.class})
public class UkOpenBankingApiDefinitionsTest {

    public static class ConsentStatusTest {
        @Test
        public void shouldReturnProperInstanceOfEnum() {
            ConsentStatus status = ConsentStatus.fromString("Rejected");
            assertThat(status).isEqualTo(ConsentStatus.REJECTED);
        }

        @Test
        public void shouldThrowExceptionWhenNullStringProvided() {
            assertThatThrownBy(() -> ConsentStatus.fromString(null))
                    .isInstanceOf(SessionError.CONSENT_INVALID.exception().getClass());
        }

        @Test
        public void shouldThrowExceptionForUnknownInstanceOfEnum() {
            assertThatThrownBy(() -> ConsentStatus.fromString("undefined"))
                    .isInstanceOf(SessionError.CONSENT_INVALID.exception().getClass());
        }
    }

    public static class BalanceTypeMapperTest {
        @Test
        public void shouldReturnProperInstanceOfAccountBalanceType() {
            AccountBalanceType accountBalanceType =
                    BalanceTypeMapper.toTinkAccountBalanceType(UkObBalanceType.CLEARED_BALANCE);

            assertThat(accountBalanceType).isEqualTo(AccountBalanceType.CLEARED_BALANCE);
        }

        @Test
        public void shouldThrowExceptionWhenNullIsProvided() {
            assertThatThrownBy(() -> BalanceTypeMapper.toTinkAccountBalanceType(null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
