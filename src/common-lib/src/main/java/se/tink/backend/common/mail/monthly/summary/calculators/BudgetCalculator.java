package se.tink.backend.common.mail.monthly.summary.calculators;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.mail.monthly.summary.model.BudgetData;
import se.tink.backend.common.mail.monthly.summary.utils.IconsUtil;
import se.tink.backend.common.search.TransactionsSearcher;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;

public class BudgetCalculator {

    private CategoryConfiguration categoryConfiguration;

    public BudgetCalculator(CategoryConfiguration categoryConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
    }

    public List<BudgetData> getBudgetData(List<FollowItem> followItems,
            Date currentPeriodEndDate, User user, TransactionsSearcher transactionsSearcher, String lastPeriod
            , Iterable<Transaction> transactions, List<Account> accounts, List<Statistic> statistics,
            List<Category> categories) {

        followItems = FluentIterable.from(followItems).filter(followItem -> followItem.getType() == FollowTypes.EXPENSES
                || followItem.getType() == FollowTypes.SEARCH).toList();

        final ImmutableMap<String, Category> categoriesById = Maps.uniqueIndex(categories,
                c -> (c.getId()));

        List<BudgetData> budgets = Lists.newArrayList();

        if (followItems != null && followItems.size() > 0) {
            Map<String, List<Transaction>> transactionsBySearchFollowItemId = FollowUtils
                    .querySearchFollowItemsTransactions(followItems, user, transactionsSearcher);

            FollowUtils.populateFollowItems(followItems, lastPeriod, lastPeriod, currentPeriodEndDate, true, false,
                    false, user, Lists.newArrayList(transactions), transactionsBySearchFollowItemId, accounts,
                    statistics, categories, categoryConfiguration);

            for (FollowItem followItem : followItems) {
                Double progress = followItem.getProgress();

                if (progress == null) {
                    continue;
                }

                int pct = Math.min((int) (Math.floor(progress * 10) * 10), 100);

                String icon;

                if (followItem.getType() == FollowTypes.SEARCH) {
                    icon = IconsUtil.getSearchIcon();
                } else {

                    if (Strings.isNullOrEmpty(followItem.getCriteria())) {
                        continue;
                    }

                    ExpensesFollowCriteria expensesFollowCriteria = SerializationUtils.deserializeFromString(
                            followItem.getCriteria(), ExpensesFollowCriteria.class);

                    if (expensesFollowCriteria == null || expensesFollowCriteria.getCategoryIds() == null) {
                        continue;
                    }

                    Category category = categoriesById.get(expensesFollowCriteria.getCategoryIds().get(0));

                    if (!Strings.isNullOrEmpty(category.getSecondaryName())) {
                        category = categoriesById.get(category.getParent());
                    }

                    icon = IconsUtil.getIcon(category.getCode());
                }

                BudgetData content = new BudgetData(followItem.getName(), icon, pct, followItem.getType());

                budgets.add(content);
            }

        }

        return budgets;
    }

}
