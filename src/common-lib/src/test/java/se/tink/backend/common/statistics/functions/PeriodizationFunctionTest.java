package se.tink.backend.common.statistics.functions;

import java.text.ParseException;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.common.statistics.StatisticsGeneratorAggregator;
import se.tink.backend.common.statistics.StatisticsGeneratorFunctions;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ThreadSafeDateFormat;

import com.google.common.collect.Lists;

public class PeriodizationFunctionTest {
    
    @Test
    public void testWeeklyPeriodization() throws ParseException {
        
        UserData userData = new UserData();
        
        User user = new User();
        
        UserProfile profile = new UserProfile();
        profile.setLocale("sv_SE");
        
        user.setProfile(profile);
        
        Credentials c1 = new Credentials();
        
        Account a1 = new Account();
        a1.setCredentialsId(c1.getId());
        
        Transaction t1 = new Transaction();
        
        // swe week 12, us week 13
        
        t1.setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2014-03-23"));
        t1.setDescription("trans 1");
        t1.setAccountId(a1.getId());
        t1.setCredentialsId(c1.getId());
        
        userData.setCredentials(Lists.newArrayList(c1));
        userData.setAccounts(Lists.newArrayList(a1));
        userData.setTransactions(Lists.newArrayList(Lists.newArrayList(t1)));
        userData.setUser(user);

        List<Statistic> statistcs = StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                Statistic.Types.EXPENSES_BY_CATEGORY, ResolutionTypes.WEEKLY, userData.getTransactions(),
                userData.getTransactions(),
                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION, null,
                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_FUNCTION);
        
        
        for (Statistic s : statistcs) {
            Assert.assertEquals("2014:12", s.getPeriod());
        }
    }
    
    @Test
    public void testWeeklyPeriodizationUs() throws ParseException {
        
        UserData userData = new UserData();
        
        User user = new User();
        
        UserProfile profile = new UserProfile();
        profile.setLocale("en_US");
        
        user.setProfile(profile);
        
        Credentials c1 = new Credentials();
        
        Account a1 = new Account();
        a1.setCredentialsId(c1.getId());
        
        Transaction t1 = new Transaction();
        
        // swe week 12, us week 13
        t1.setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2014-03-23"));
        t1.setDescription("trans 1");
        t1.setAccountId(a1.getId());
        t1.setCredentialsId(c1.getId());
        
        userData.setCredentials(Lists.newArrayList(c1));
        userData.setAccounts(Lists.newArrayList(a1));
        userData.setTransactions(Lists.newArrayList(Lists.newArrayList(t1)));
        userData.setUser(user);

        List<Statistic> statistics = StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(
                userData, Statistic.Types.EXPENSES_BY_CATEGORY, ResolutionTypes.WEEKLY, userData.getTransactions(),
                userData.getTransactions(),
                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION, null,
                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_FUNCTION);
        
        
        for (Statistic s : statistics) {
            Assert.assertEquals("2014:13", s.getPeriod());
        }
    }
}
