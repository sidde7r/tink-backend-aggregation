package se.tink.backend.common.utils;

import com.google.common.collect.Sets;
import java.util.Optional;
import se.tink.backend.core.CassandraUserDemographics;
import se.tink.backend.core.UserDemographics;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraUserDemographicsConverter {
    public static CassandraUserDemographics toCassandra(UserDemographics t) {
        final CassandraUserDemographics c = new CassandraUserDemographics();

        c.setAge(t.getAge());
        c.setBirth(t.getBirth());
        c.setCity(t.getCity());
        c.setPostalcode(t.getPostalCode());
        c.setCommunity(t.getCommunity());
        c.setCountry(t.getCountry());
        c.setCreated(t.getCreated());
        c.setCredentialsCount(t.getCredentialsCount());
        c.setCurrentCategorization(t.getCurrentCategorization());
        c.setDeleted(t.getDeleted());
        c.setFirstUpdatedEvent(t.getFirstUpdatedEvent());
        c.setFlags(t.getFlags());
        c.setFollowItemCount(t.getFollowItemCount());
        c.setGender(t.getGender());
        c.setHasFacebook(t.isHasFacebook());
        c.setHasPassword(t.isHasPassword());
        c.setIncome(t.getIncome());
        c.setInitialCategorization(t.getInitialCategorization());
        c.setLastUpdatedEvent(t.getLastUpdatedEvent());
        c.setMarket(t.getMarket());
        c.setProviders(Sets.newHashSet(t.getProviders()));
        c.setTaggedTransactionCount(t.getTaggedTransactionCount());
        c.setTransactionCount(t.getTransactionCount());
        c.setUniqueTagCount(t.getUniqueTagCount());
        c.setUserId(Optional.ofNullable(t.getUserId()).map(UUIDUtils.FROM_TINK_UUID_TRANSFORMER::apply).orElse
                (null));
        c.setValidCleanDataPeriodsCount(t.getValidCleanDataPeriodsCount());
        c.setValidCredentialsCount(t.getValidCredentialsCount());
        c.setWeeklyAuthErrorFrequency(t.getWeeklyAuthErrorFrequency());
        c.setWeeklyErrorFrequency(t.getWeeklyErrorFrequency());
        c.setCampaign(t.getCampaign());
        c.setSource(t.getSource());
        c.setOrganic(t.isOrganic());
        c.setHasHadTransactions(t.isHasHadTransactions());

        return c;
    }
}
