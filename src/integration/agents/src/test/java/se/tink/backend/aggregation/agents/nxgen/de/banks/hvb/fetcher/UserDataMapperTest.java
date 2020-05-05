package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import se.tink.libraries.identitydata.IdentityData;

public class UserDataMapperTest {

    private static final String GIVEN_NAME = "Test name";
    private static final String GIVEN_SURNAME = "Test surname";
    private static final String GIVEN_FULL_NAME = GIVEN_NAME + " " + GIVEN_SURNAME;
    private static final String GIVEN_BANKING_ID_WITH_DATEOFBIRTH_IN_20TH_CENTURY = "1234101090";
    private static final LocalDate EXPECTED_DATE_OF_BIRTH_IN_20TH_CENTURY =
            LocalDate.of(1990, 10, 10);
    private static final String GIVEN_BANKING_ID_WITH_DATEOFBIRTH_IN_21ST_CENTURY = "1234101005";
    private static final LocalDate EXPECTED_DATE_OF_BIRTH_IN_21ST_CENTURY =
            LocalDate.of(2005, 10, 10);

    private UserDataMapper tested = new UserDataMapper();

    @Test
    public void toIdentityDataShouldReturnValidObjectForUserBornIn20thCentury() {
        // given
        UserDataResponse giveUserDataResponse = givenStandardUserBornIn20thCenturyDataResponse();

        // when
        IdentityData identityData = tested.toIdentityData(giveUserDataResponse);

        // then
        assertThat(identityData.getFullName()).isEqualTo(GIVEN_FULL_NAME);
        assertThat(identityData.getDateOfBirth())
                .isEqualTo(EXPECTED_DATE_OF_BIRTH_IN_20TH_CENTURY.toString());
    }

    @Test
    public void toIdentityDataShouldReturnValidObjectForUserBornIn21stCentury() {
        // given
        UserDataResponse giveUserDataResponse = givenStandardUserBornIn21stCenturyDataResponse();

        // when
        IdentityData identityData = tested.toIdentityData(giveUserDataResponse);

        // then
        assertThat(identityData.getFullName()).isEqualTo(GIVEN_FULL_NAME);
        assertThat(identityData.getDateOfBirth())
                .isEqualTo(EXPECTED_DATE_OF_BIRTH_IN_21ST_CENTURY.toString());
    }

    @Test
    public void toIdentityDataShouldThrowExceptionForResponseWithoutDetails() {
        // given
        UserDataResponse giveUserDataResponse =
                givenUserBornIn20thCenturyWithoutDetailsDataResponse();

        // when
        ThrowableAssert.ThrowingCallable callable =
                () -> tested.toIdentityData(giveUserDataResponse);

        // then
        assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
    }

    private UserDataResponse givenStandardUserBornIn20thCenturyDataResponse() {
        return new UserDataResponse()
                .setUser(givenUser())
                .setDetails(givenStandardResponseDetailsForUserBornIn20thCentury());
    }

    private UserDataResponse givenStandardUserBornIn21stCenturyDataResponse() {
        return new UserDataResponse()
                .setUser(givenUser())
                .setDetails(givenStandardResponseDetailsForUserBornIn21stCentury());
    }

    private UserDataResponse givenUserBornIn20thCenturyWithoutDetailsDataResponse() {
        return new UserDataResponse()
                .setUser(givenUser())
                .setDetails(givenEmptyResponseDetailsForUserBornIn20thCentury());
    }

    private List<UserDataResponse.ResponseDetails>
            givenStandardResponseDetailsForUserBornIn20thCentury() {
        return Collections.singletonList(
                new UserDataResponse.ResponseDetails()
                        .setDirectBankingOwner(GIVEN_FULL_NAME)
                        .setDirectBankingNumber(GIVEN_BANKING_ID_WITH_DATEOFBIRTH_IN_20TH_CENTURY));
    }

    private List<UserDataResponse.ResponseDetails>
            givenStandardResponseDetailsForUserBornIn21stCentury() {
        return Collections.singletonList(
                new UserDataResponse.ResponseDetails()
                        .setDirectBankingOwner(GIVEN_FULL_NAME)
                        .setDirectBankingNumber(GIVEN_BANKING_ID_WITH_DATEOFBIRTH_IN_21ST_CENTURY));
    }

    private List<UserDataResponse.ResponseDetails>
            givenEmptyResponseDetailsForUserBornIn20thCentury() {
        return Collections.emptyList();
    }

    private UserDataResponse.User givenUser() {
        return new UserDataResponse.User().setName(GIVEN_NAME).setSurname(GIVEN_SURNAME);
    }
}
