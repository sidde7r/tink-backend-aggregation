package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class AccountDetailsResponseTest {

    private final String ZERO_INTEREST_RATE = "0.000";
    private final String INTEREST_RATE = "1.123";

    @Test
    public void shouldReturnZeroWhenAccountInterestDetailsIsNull() {
        // given
        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();

        // when
        Double result = accountDetailsResponse.getInterestRate();

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void shouldReturnZeroWhenInterestDetailsListIsEmpty() {
        // given
        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setAccountInterestDetails(new AccountInterestDetailsEntity());

        // when
        Double result = accountDetailsResponse.getInterestRate();

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void shouldGetFirstInterestRateHigherThanZero() {
        // given
        InterestDetailEntity interestDetailEntity1 = new InterestDetailEntity();
        interestDetailEntity1.setRateInPercent(ZERO_INTEREST_RATE);
        InterestDetailEntity interestDetailEntity2 = new InterestDetailEntity();
        interestDetailEntity2.setRateInPercent(INTEREST_RATE);

        AccountInterestDetailsEntity accountInterestDetailsEntity =
                new AccountInterestDetailsEntity();
        accountInterestDetailsEntity.setInterestDetails(
                Arrays.asList(interestDetailEntity1, interestDetailEntity2));

        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setAccountInterestDetails(accountInterestDetailsEntity);

        // when
        Double result = accountDetailsResponse.getInterestRate();

        // then
        assertThat(result).isEqualTo(0.01123);
    }

    @Test
    @Parameters(method = "ratesParams")
    public void shouldReturnZeroWhenRateIsWrongFormat(String firstRate, String secondRate) {
        // given
        InterestDetailEntity interestDetailEntity1 = new InterestDetailEntity();
        interestDetailEntity1.setRateInPercent(firstRate);
        InterestDetailEntity interestDetailEntity2 = new InterestDetailEntity();
        interestDetailEntity2.setRateInPercent(secondRate);

        AccountInterestDetailsEntity accountInterestDetailsEntity =
                new AccountInterestDetailsEntity();
        accountInterestDetailsEntity.setInterestDetails(
                Arrays.asList(interestDetailEntity1, interestDetailEntity2));

        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setAccountInterestDetails(accountInterestDetailsEntity);

        // when
        Double result = accountDetailsResponse.getInterestRate();

        // then
        assertThat(result).isEqualTo(0.0);
    }

    private Object[] ratesParams() {
        return new Object[] {
            new Object[] {ZERO_INTEREST_RATE, ZERO_INTEREST_RATE},
            new Object[] {null, INTEREST_RATE},
            new Object[] {"rate", INTEREST_RATE},
            new Object[] {"", ""},
            new Object[] {"", INTEREST_RATE},
            new Object[] {ZERO_INTEREST_RATE, ""},
        };
    }
}
