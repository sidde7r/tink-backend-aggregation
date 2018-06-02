package se.tink.backend.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Currency;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;

/**
 * TODO this is probably irrelevant
 */
public class ActivityGeneratorWorkerTestBase extends AbstractServiceIntegrationTest {

    protected List<ActivityGeneratorContext> getActivityGeneratorContext() {
        List<User> users = getTestUsers("Test User");
        List<ActivityGeneratorContext> contexts = users.stream().map(u -> {
            ActivityGeneratorContext context = new ActivityGeneratorContext();
            context.setCategoryConfiguration(serviceContext.getCategoryConfiguration());
            context.setUser(u);

            Locale locale = new Locale("sv", "SE");
            context.setLocale(locale);

            Currency currency = new Currency();
            currency.setCode("SEK");
            Map<String, Currency> currencies = Maps.newHashMap();
            currencies.put("SEK", currency);
            context.setCurrencies(currencies);

            context.setCatalog(new Catalog(locale));
            context.setCategories(categoryRepository.findLeafCategories());
            context.setCategoriesByCodeForLocale(Maps.uniqueIndex(context.getCategories(),
                    Category::getCode));
            context.setStatistics(getStatistics(u.getId()));
            context.setAccounts(getAccounts(u));
            context.setFollowItems(getFollowsItem());
            context.setTransactions(
                    getTestTransactionsWithCategoryType(u.getId(), context.getCategoriesByCodeForLocale()));
            context.setUserState(getUserState(u.getId()));
            context.setCategoriesById(Maps.uniqueIndex(context.getCategoriesByCodeForLocale().values(),
                    Category::getId));
            context.setCluster(serviceContext.getConfiguration().getCluster());
            context.setActivitiesConfiguration(serviceContext.getConfiguration().getActivities());

            return context;
        }).collect(Collectors.toList());
        return contexts;
    }

    private List<Statistic> getStatistics(String userId) {
        String period = "2016-07";

        Statistic statistic1 = new Statistic();
        statistic1.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic1.setPeriod(period);
        statistic1.setUserId(userId);
        statistic1.setType("left-to-spend");
        statistic1.setDescription("2016-06-23");
        statistic1.setValue(50000);

        Statistic statistic2 = new Statistic();
        statistic2.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic2.setPeriod(period);
        statistic2.setUserId(userId);
        statistic2.setType("left-to-spend");
        statistic2.setDescription("2016-06-24");
        statistic2.setValue(50000);

        Statistic statistic3 = new Statistic();
        statistic3.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic3.setPeriod(period);
        statistic3.setUserId(userId);
        statistic3.setType("left-to-spend");
        statistic3.setDescription("2016-06-25");
        statistic3.setValue(49261);

        Statistic statistic4 = new Statistic();
        statistic4.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic4.setPeriod(period);
        statistic4.setUserId(userId);
        statistic4.setType("left-to-spend");
        statistic4.setDescription("2016-06-26");
        statistic4.setValue(49261);

        Statistic statistic5 = new Statistic();
        statistic5.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic5.setPeriod(period);
        statistic5.setUserId(userId);
        statistic5.setType("left-to-spend");
        statistic5.setDescription("2016-06-27");
        statistic5.setValue(49261);

        Statistic statistic6 = new Statistic();
        statistic6.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic6.setPeriod(period);
        statistic6.setUserId(userId);
        statistic6.setType("left-to-spend");
        statistic6.setDescription("2016-06-28");
        statistic6.setValue(4261);

        Statistic statistic7 = new Statistic();
        statistic7.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic7.setPeriod(period);
        statistic7.setUserId(userId);
        statistic7.setType("left-to-spend");
        statistic7.setDescription("2016-06-29");
        statistic7.setValue(4261);

        Statistic statistic8 = new Statistic();
        statistic8.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic8.setPeriod(period);
        statistic8.setUserId(userId);
        statistic8.setType("left-to-spend");
        statistic8.setDescription("2016-06-30");
        statistic8.setValue(4261);

        Statistic statistic9 = new Statistic();
        statistic9.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic9.setPeriod(period);
        statistic9.setUserId(userId);
        statistic9.setType("income-and-expenses");
        statistic9.setDescription("INCOME");
        statistic9.setValue(50000);

        Statistic statistic10 = new Statistic();
        statistic10.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic10.setPeriod(period);
        statistic10.setUserId(userId);
        statistic10.setType("left-to-spend-average");
        statistic10.setDescription("EXPENSES");
        statistic10.setValue(4261);

        Statistic statistic11 = new Statistic();
        statistic11.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic11.setPeriod(period);
        statistic11.setUserId(userId);
        statistic11.setType("income-and-expenses");
        statistic11.setDescription("EXPENSES");
        statistic11.setValue(-45739);

        List<Statistic> statistics = com.google.api.client.util.Lists.newArrayList();
        statistics.add(statistic1);
        statistics.add(statistic2);
        statistics.add(statistic3);
        statistics.add(statistic4);
        statistics.add(statistic5);
        statistics.add(statistic6);
        statistics.add(statistic7);
        statistics.add(statistic8);
        statistics.add(statistic9);
        statistics.add(statistic10);
        statistics.add(statistic11);

        return statistics;
    }

    private List<Account> getAccounts(User user) {
        UUID accountUUID = UUID.randomUUID();
        String ACCOUNTID = UUIDUtils.toTinkUUID(accountUUID);

        Account account = new Account();
        account.setBalance(550);
        account.setName(user.getUsername());
        account.setUserId(user.getId());
        account.setId(ACCOUNTID);

        List<Account> accounts = new ArrayList<>();
        accounts.add(account);

        return accounts;
    }

    private List<FollowItem> getFollowsItem() {
        List<FollowItem> followItems = com.google.api.client.util.Lists.newArrayList();

        List<Category> categories = categoryRepository.findLeafCategories();

        ExpensesFollowCriteria followCriteria = new ExpensesFollowCriteria();
        followCriteria.setCategoryIds(Lists.newArrayList(categories.get(0).getId()));
        followCriteria.setTargetAmount(5000d);

        FollowItem item = new FollowItem();
        item.setName("IKEA");
        item.setType(FollowTypes.EXPENSES);
        item.setCriteria(SerializationUtils.serializeToString(followCriteria));
        followItems.add(item);

        return followItems;
    }

    protected List<Transaction> getTestTransactionsWithCategoryType(String userId,
            Map<String, Category> categoriesByCodeForLocale) {
        List<Transaction> transactions = getTestTransactions(userId);

        for (Transaction transaction : transactions) {

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = formatter.parse("2016-06-25");
                transaction.setDate(date);
                transaction.setCategory(categoriesByCodeForLocale.get(categoryConfiguration.getRestaurantsCode()));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            Date date = formatter.parse("2016-06-23");
            Transaction income = getNewTransaction(userId, 50000, "Lön");
            income.setDate(date);
            income.setCategory(categoriesByCodeForLocale.get(categoryConfiguration.getIncomeUnknownCode()));
            transactions.add(income);

            date = formatter.parse("2016-06-28");
            Transaction bigExpense = getNewTransaction(userId, -45000, "IKEA");
            bigExpense.setDate(date);
            bigExpense.setCategory(categoriesByCodeForLocale.get(categoryConfiguration.getElectronicsCode()));
            transactions.add(bigExpense);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private UserState getUserState(String userId) {
        Period period = new Period();
        period.setClean(true);
        period.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = formatter.parse("2016-06-23");
            Date endDate = formatter.parse("2016-06-30");
            period.setStartDate(startDate);
            period.setEndDate(endDate);
            period.setName("2016-07");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<Period> periods = com.google.api.client.util.Lists.newArrayList();
        periods.add(period);

        UserState userState = new UserState();
        userState.setUserId(userId);
        userState.setPeriods(periods);

        return userState;
    }
}
