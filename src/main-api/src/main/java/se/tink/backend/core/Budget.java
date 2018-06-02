package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowData;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "budgets")
public class Budget {

    private static final LogUtils log = new LogUtils(Budget.class);

    /**
     * Helper method to calculate the average historical value.
     * 
     * @param followItemData
     */
    private static double calculateAverageHistoricalAmount(FollowData followItemData) {
        if (followItemData.getHistoricalAmounts() == null || followItemData.getHistoricalAmounts().isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (StringDoublePair historicalAmount : followItemData.getHistoricalAmounts()) {
            sum += historicalAmount.getValue();
        }

        return sum / followItemData.getHistoricalAmounts().size();
    }

    /**
     * Helper function to convert a Budget into a FollowItem.
     * 
     * @param budget
     * @return
     */
    public static FollowItem convertBudget(Budget budget, User user, Map<String, Category> categoriesById) {
        ExpensesFollowCriteria filter = new ExpensesFollowCriteria();
        filter.setCategoryIds(Lists.newArrayList(budget.getCategoryId()));
        filter.setTargetAmount(-budget.getBudgetedAmount());

        FollowItem followItem = new FollowItem();

        Category category = categoriesById.get(budget.getCategoryId());

        followItem.setName(category.getSecondaryName() != null ? category.getSecondaryName() : category
                .getPrimaryName());
        followItem.setId(budget.getId());
        followItem.setUserId(budget.getUserId());
        followItem.setType(FollowTypes.EXPENSES);
        followItem.setCriteria(SerializationUtils.serializeToString(filter));

        return followItem;
    }

    public static List<Budget> convertFollowItems(List<FollowItem> followItems, final User user,
            final Map<String, Category> categoriesById) {
        Predicate<FollowItem> validBudgetFollowPredicate = f -> {
            if (f.getType() != FollowTypes.EXPENSES) {
                return false;
            }

            ExpensesFollowCriteria filter = SerializationUtils.deserializeFromString(f.getCriteria(),
                    ExpensesFollowCriteria.class);

            if (f.getId() != null && filter.getTargetAmount() == null) {
                return false;
            }

            List<String> categoryIds = filter.getCategoryIds();

            if (categoryIds == null || categoryIds.size() != 1) {
                return false;
            }

            String firstCategoryId = filter.getCategoryIds().get(0);

            Category category = categoriesById.get(firstCategoryId);

            if (category == null) {
                log.error(user.getId(), String.format("Did not find category with id: %s", firstCategoryId));
                return false;
            }

            if (Strings.isNullOrEmpty(category.getSecondaryName())) {
                return false;
            }

            return true;
        };

        Iterable<FollowItem> filteredFollowItems = Iterables.filter(followItems, validBudgetFollowPredicate);

        if (Iterables.isEmpty(filteredFollowItems)) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(Iterables.filter(
                Iterables.transform(filteredFollowItems, f -> (Budget.convertFollowItem(f, user))), b -> (b != null)));
    }

    /**
     * Helper function to convert a FollowItem to a Budget.
     * 
     * @param followItem
     * @return
     */
    public static Budget convertFollowItem(FollowItem followItem, final User user) {
        final ExpensesFollowCriteria followFilter = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                ExpensesFollowCriteria.class);
        final Budget budget = new Budget();

        budget.setId(followItem.getId());
        budget.setUserId(followItem.getUserId());
        budget.setCategoryId(followFilter.getCategoryIds().get(0));

        budget.setBudgetedAmount(followFilter.getTargetAmount() != null ? Math.abs(followFilter.getTargetAmount()) : 0);

        final FollowData followData = followItem.getData();

        if (followData != null) {
            budget.setSuggestedAmount(Math.ceil(Math.abs(calculateAverageHistoricalAmount(followData)) / 100) * 100);

            budget.setHistoricalAmounts(Lists.newArrayList(Iterables.transform(followData.getHistoricalAmounts(),
                    d -> {
                        Statistic s = new Statistic();

                        s.setPeriod(d.getKey());

                        s.setResolution(user.getProfile().getPeriodMode());
                        s.setDescription(budget.getCategoryId());
                        s.setUserId(user.getId());
                        s.setType(Statistic.Types.EXPENSES_BY_CATEGORY);
                        s.setValue(Math.abs(d.getValue()));

                        return s;
                    })));

            StringDoublePair lastAmount = Iterables.getLast(followData.getHistoricalAmounts(), null);

            if (lastAmount != null) {
                budget.setCurrentAmount(Math.abs(lastAmount.getValue()));
            }
        }

        return budget;
    }

    @Creatable
    @Modifiable
    protected double budgetedAmount;
    @Modifiable
    @Creatable
    protected String categoryId;
    @Transient
    protected double currentAmount;
    @Transient
    protected List<Statistic> historicalAmounts;
    @Id
    protected String id;
    @Transient
    protected double suggestedAmount;
    protected String userId;

    public Budget() {
        id = StringUtils.generateUUID();
    }

    public double getBudgetedAmount() {
        return this.budgetedAmount;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public double getCurrentAmount() {
        return this.currentAmount;
    }

    public List<Statistic> getHistoricalAmounts() {
        return historicalAmounts;
    }

    public String getId() {
        return this.id;
    }

    public double getSuggestedAmount() {
        return suggestedAmount;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setBudgetedAmount(double budgetedAmount) {
        this.budgetedAmount = budgetedAmount;
    }

    public void setCategoryId(String category) {
        this.categoryId = category;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setHistoricalAmounts(List<Statistic> historicalAmounts) {
        this.historicalAmounts = historicalAmounts;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSuggestedAmount(double suggestedAmount) {
        this.suggestedAmount = suggestedAmount;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("categoryId", categoryId)
                .add("budgetedAmount", budgetedAmount).add("currentAmount", currentAmount)
                .add("suggestedAmount", suggestedAmount).add("historicalAmounts", historicalAmounts).toString();
    }
}
