package se.tink.backend.backend.utils.guavaimpl;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.utils.guavaimpl.Orderings;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class OrderingsTest {
    public static class CredentialsByTypeAndUpdated {
        @Test
        public void updatedDateIsAfterANullDate() {
            Credentials hasUpdated = new Credentials();
            hasUpdated.setUpdated(createDate());

            Credentials withoutUpdated = new Credentials();
            withoutUpdated.setUpdated(null);

            Credentials max = ImmutableList.of(hasUpdated, withoutUpdated).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(hasUpdated);
        }

        @Test
        public void updatedMostRecentDateIsLast() {
            Credentials hasDate = new Credentials();
            Date firstDate = createDate();
            hasDate.setUpdated(firstDate);

            Credentials hasMoreRecentDate = new Credentials();
            hasMoreRecentDate.setUpdated(createLaterDate(firstDate));

            Credentials max = ImmutableList.of(hasDate, hasMoreRecentDate).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(hasMoreRecentDate);
        }

        @Test
        public void statusUpdatedDateIsAfterANullDate() {
            Credentials hasUpdated = new Credentials();
            hasUpdated.setStatusUpdated(createDate());

            Credentials withoutUpdated = new Credentials();
            withoutUpdated.setStatusUpdated(null);

            Credentials max = ImmutableList.of(hasUpdated, withoutUpdated).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(hasUpdated);
        }

        @Test
        public void statusUpdatedMostRecentDateIsLast() {
            Credentials hasDate = new Credentials();
            Date firstDate = createDate();
            hasDate.setStatusUpdated(firstDate);

            Credentials hasMoreRecentDate = new Credentials();
            hasMoreRecentDate.setStatusUpdated(createLaterDate(firstDate));

            Credentials max = ImmutableList.of(hasDate, hasMoreRecentDate).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(hasMoreRecentDate);
        }

        @Test
        public void typeBankIdIsAfterPassword() {
            Credentials bankId = new Credentials();
            bankId.setType(CredentialsTypes.MOBILE_BANKID);

            Credentials password = new Credentials();
            password.setType(CredentialsTypes.PASSWORD);

            Credentials max = ImmutableList.of(bankId, password).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();
            assertThat(max).isEqualTo(bankId);
        }

        @Test
        public void typePasswordIsAfterNull() {
            Credentials nullType = new Credentials();
            nullType.setType(null);

            Credentials password = new Credentials();
            password.setType(CredentialsTypes.PASSWORD);

            Credentials max = ImmutableList.of(password, nullType).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();
            assertThat(max).isEqualTo(password);
        }

        @Test
        public void typeIsComparedBeforeUpdated() {
            Credentials bankIdWithFirstDate = new Credentials();
            Date firstDate = createDate();
            bankIdWithFirstDate.setType(CredentialsTypes.MOBILE_BANKID);
            bankIdWithFirstDate.setUpdated(firstDate);

            Credentials passwordWithMoreRecentDate = new Credentials();
            passwordWithMoreRecentDate.setType(CredentialsTypes.PASSWORD);
            passwordWithMoreRecentDate.setUpdated(createLaterDate(firstDate));

            Credentials max = ImmutableList.of(passwordWithMoreRecentDate, bankIdWithFirstDate).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();
            assertThat(max).isEqualTo(bankIdWithFirstDate);
        }

        @Test
        public void updatedIsComparedBeforeStatusUpdated() {
            Date firstDate = createDate();
            Date moreRecentDate = createLaterDate(firstDate);

            Credentials expectedMax = new Credentials();
            expectedMax.setUpdated(moreRecentDate);
            expectedMax.setStatusUpdated(firstDate);

            Credentials expectedMin = new Credentials();
            expectedMin.setUpdated(firstDate);
            expectedMin.setUpdated(moreRecentDate);

            Credentials max = ImmutableList.of(expectedMax, expectedMin).stream()
                    .sorted(Orderings.CREDENTIALS_BY_TYPE_AND_ACTIVITY.reversed()).findFirst().get();
            assertThat(max).isEqualTo(expectedMax);
        }

        private static Date createDate() {
            return new DateTime(2016, 1, 1, 12, 0, 0, DateTimeZone.UTC).toDate();
        }

        private static Date createLaterDate(Date date) {
            return new DateTime(date).plusSeconds(1).toDate();
        }
    }

    public static class CredentialsByCreditSafe {
        @Test
        public void creditSafeIsMax() {
            Credentials creditSafeProvider = new Credentials();
            creditSafeProvider.setProviderName("creditsafe");

            Credentials nullProvider = new Credentials();
            nullProvider.setProviderName(null);

            Credentials otherProvider = new Credentials();
            otherProvider.setProviderName("other-provider");

            Credentials max = ImmutableList.of(nullProvider, creditSafeProvider, otherProvider).stream()
                    .sorted(Orderings.CREDENTIALS_BY_CREDITSAFE.reversed())
                    .findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(creditSafeProvider);
        }
    }

    public static class CredentialsByUsername {
        @Test
        public void credentialWithSameUsernameIsMax() {
            Credentials sameUsername = new Credentials();
            sameUsername.setField(Field.Key.USERNAME, "user@user.se");

            Credentials nullUsername = new Credentials();
            nullUsername.setField(Field.Key.USERNAME, null);

            Credentials otherUsername = new Credentials();
            otherUsername.setField(Field.Key.USERNAME, "other@other.se");

            Credentials max = ImmutableList.of(nullUsername, sameUsername, otherUsername).stream().sorted(Orderings
                    .credentialsByUsername("user@user.se").reversed()).findFirst().get();

            assertThat(max).isEqualToComparingFieldByField(sameUsername);
        }
    }
}
