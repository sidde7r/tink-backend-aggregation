package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.enums.MarketCode;

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

    @Test
    @Parameters(method = "accountOwnersParams")
    public void shouldReturnAccountOwners(
            List<String> accountOwners, String marketCode, List<String> expected) {
        // given
        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setAccountOwners(accountOwners);

        // when
        List<String> result = accountDetailsResponse.getAccountOwners(marketCode);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] accountOwnersParams() {
        String FIRST_ACCOUNT_OWNER = "NAME LASTNAME";
        String SECOND_ACCOUNT_OWNER = "SECOND NAME LASTNAME";
        String ACCOUNT_OWNER_WITH_SSN = "12345678901 - NAME LASTNAME";
        String SECOND_ACCOUNT_OWNER_WITH_SSN = "1234512 - SECOND NAME LASTNAME";

        return new Object[] {
            new Object[] {
                Arrays.asList(FIRST_ACCOUNT_OWNER, SECOND_ACCOUNT_OWNER),
                MarketCode.DK.name(),
                Arrays.asList(FIRST_ACCOUNT_OWNER, SECOND_ACCOUNT_OWNER)
            },
            new Object[] {
                Arrays.asList("", SECOND_ACCOUNT_OWNER),
                MarketCode.DK.name(),
                Collections.singletonList(SECOND_ACCOUNT_OWNER)
            },
            new Object[] {
                Arrays.asList(null, SECOND_ACCOUNT_OWNER),
                MarketCode.DK.name(),
                Collections.singletonList(SECOND_ACCOUNT_OWNER)
            },
            new Object[] {
                Arrays.asList(ACCOUNT_OWNER_WITH_SSN, SECOND_ACCOUNT_OWNER_WITH_SSN),
                MarketCode.SE.name(),
                Arrays.asList(FIRST_ACCOUNT_OWNER, SECOND_ACCOUNT_OWNER)
            },
            new Object[] {
                Arrays.asList(ACCOUNT_OWNER_WITH_SSN, SECOND_ACCOUNT_OWNER_WITH_SSN),
                MarketCode.NO.name(),
                Arrays.asList(FIRST_ACCOUNT_OWNER, SECOND_ACCOUNT_OWNER)
            },
            new Object[] {Collections.emptyList(), MarketCode.DK.name(), Collections.emptyList()},
            new Object[] {Collections.emptyList(), MarketCode.SE.name(), Collections.emptyList()},
            new Object[] {
                Arrays.asList(FIRST_ACCOUNT_OWNER, SECOND_ACCOUNT_OWNER_WITH_SSN),
                MarketCode.NO.name(),
                Collections.emptyList()
            },
            new Object[] {
                Arrays.asList("", FIRST_ACCOUNT_OWNER),
                MarketCode.NO.name(),
                Collections.emptyList()
            },
            new Object[] {
                Arrays.asList(null, FIRST_ACCOUNT_OWNER),
                MarketCode.NO.name(),
                Collections.emptyList()
            },
            new Object[] {
                Arrays.asList("", ACCOUNT_OWNER_WITH_SSN),
                MarketCode.NO.name(),
                Collections.singletonList(FIRST_ACCOUNT_OWNER)
            },
            new Object[] {
                Arrays.asList(null, ACCOUNT_OWNER_WITH_SSN),
                MarketCode.NO.name(),
                Collections.singletonList(FIRST_ACCOUNT_OWNER)
            },
            new Object[] {null, MarketCode.NO.name(), Collections.emptyList()},
            new Object[] {null, MarketCode.DK.name(), Collections.emptyList()},
            new Object[] {null, MarketCode.SE.name(), Collections.emptyList()},
        };
    }
}
