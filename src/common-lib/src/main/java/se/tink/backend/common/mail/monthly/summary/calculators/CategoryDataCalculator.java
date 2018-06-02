package se.tink.backend.common.mail.monthly.summary.calculators;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.mail.monthly.summary.model.CategoryData;
import se.tink.backend.core.Category;
import se.tink.backend.core.Statistic;

public class CategoryDataCalculator {

    private List<Statistic> firstPeriodStatistics;
    private List<Statistic> secondPeriodStatistics;
    private ImmutableMap<String, Category> categoriesById;

    private static final Ordering<CategoryData> CATEGORY_DATA_ORDERING = new Ordering<CategoryData>() {
        public int compare(CategoryData left, CategoryData right) {
            return Doubles.compare(right.getCurrentPeriodShare(), left.getCurrentPeriodShare());
        }
    };

    public void setFirstPeriodStatistics(List<Statistic> firstPeriodStatistics) {
        this.firstPeriodStatistics = firstPeriodStatistics;
    }

    public void setSecondPeriodStatistics(List<Statistic> secondPeriodStatistics) {
        this.secondPeriodStatistics = secondPeriodStatistics;
    }

    public List<CategoryData> getCategoryData() {

        Preconditions.checkNotNull(firstPeriodStatistics);
        Preconditions.checkNotNull(secondPeriodStatistics);
        Preconditions.checkNotNull(categoriesById);

        ImmutableListMultimap<String, Statistic> firstPeriodByParent = Multimaps.index(firstPeriodStatistics,
                s -> (categoriesById.get(s.getDescription()).getParent()));

        ImmutableListMultimap<String, Statistic> secondPeriodByParent = Multimaps.index(secondPeriodStatistics,
                s -> (categoriesById.get(s.getDescription()).getParent()));

        final double totalFirstPeriod = getTotal(firstPeriodStatistics);
        final double totalSecondPeriod = getTotal(secondPeriodStatistics);

        Map<String, CategoryData> categoryData = Maps.newHashMap();

        // Calculate for first period
        for (String parentCategoryId : firstPeriodByParent.keySet()) {
            addOrUpdateStatistics(categoryData, parentCategoryId, firstPeriodByParent.get(parentCategoryId),
                    totalFirstPeriod, true);
        }

        // Calculate for second period
        for (String parentCategoryId : secondPeriodByParent.keySet()) {
            addOrUpdateStatistics(categoryData, parentCategoryId, secondPeriodByParent.get(parentCategoryId),
                    totalSecondPeriod, false);
        }

        return FluentIterable.from(categoryData.values()).filter(categoryData1 -> !categoryData1.isEmpty())
                .toSortedList(CATEGORY_DATA_ORDERING);

    }

    private void addOrUpdateStatistics(Map<String, CategoryData> data, String key, ImmutableList<Statistic> statistics,
            double total, boolean isFirstPeriod) {

        double categoryTotal = getTotal(statistics);
        double pct = categoryTotal / total;

        CategoryData item = data.containsKey(key) ? data.get(key) : new CategoryData();
        item.setCategory(categoriesById.get(key));

        if (isFirstPeriod) {
            item.setCurrentPeriodShare(pct);
        } else {
            item.setPreviousPeriodShare(pct);
        }

        data.put(key, item);
    }

    private double getTotal(List<Statistic> statistics) {
        double result = 0;
        for (Statistic s : statistics) {
            result += Math.abs(s.getValue());
        }

        return result;
    }

    public void setCategories(List<Category> categories) {

        categoriesById = Maps.uniqueIndex(categories,
                c -> (c.getId()));
    }

}

