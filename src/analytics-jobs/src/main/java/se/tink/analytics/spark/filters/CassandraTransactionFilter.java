package se.tink.analytics.spark.filters;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.spark.api.java.function.Function;
import se.tink.backend.core.Account;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.Period;
import se.tink.backend.core.TransactionTypes;

@SuppressWarnings("serial")
public class CassandraTransactionFilter implements Function<CassandraTransaction, Boolean> {

    private final String excludedCategoryId;
    private final Set<String> excludedAccountIds;
    private final Boolean requireHasMerchant;
    private final Set<String> categoryIds;
    private final Set<CategoryTypes> categoryTypes;
    private final TransactionTypes type;
    private final Set<String> userIds;
    private final Period period;
    private final Double minAmount;
    private final Double maxAmount;

    private CassandraTransactionFilter(
            final TransactionTypes type,
            final Set<String> categoryIds,
            final Set<CategoryTypes> categoryTypes,
            final String excludedCategoryId,
            final Set<String> excludedAccountIds,
            final Boolean requireHasMerchant,
            final Set<String> userIds,
            final Period period,
            final Double minAmount,
            final Double maxAmount) {

        this.excludedCategoryId = excludedCategoryId;
        this.excludedAccountIds = excludedAccountIds;
        this.requireHasMerchant = requireHasMerchant;
        this.userIds = userIds;
        this.categoryIds = categoryIds;
        this.categoryTypes = categoryTypes;
        this.type = type;
        this.period = period;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    @Override
    public Boolean call(CassandraTransaction t) throws Exception {

        if (requireHasMerchant != null && requireHasMerchant) {
            if (Strings.isNullOrEmpty(t.getTinkMerchantId())) {
                return false;
            }
        }

        if (userIds != null) {
            if (!userIds.contains(t.getTinkUserId())) {
                return false;
            }
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            if (!categoryIds.contains(t.getTinkCategoryId())) {
                return false;
            }
        }
        
        if (categoryTypes != null) {
            if (!categoryTypes.contains(t.getCategoryType())) {
                return false;
            }
        }
        
        if (type != null) {
            if (t.getType() != type) {
                return false;
            }
        }

        if (excludedCategoryId != null) {
            if (excludedCategoryId.equals(t.getTinkCategoryId())) {
                return false;
            }
        }

        if (excludedAccountIds != null) {
            if (excludedAccountIds.contains(t.getTinkAccountId())) {
                return false;
            }
        }
        
        if (period != null) {
            if (t.getDate() == null) {
                return false;
            }

            if (period.getStartDate().after(t.getDate())) {
                return false;
            }

            if (period.getEndDate().before(t.getDate())) {
                return false;
            }
        }
        
        if (minAmount != null) {
            if (BigDecimal.valueOf(minAmount).abs().compareTo(t.getOriginalAmount().abs()) > 0) {
                return false;
            }
        }
        
        if (maxAmount != null) {
            if (BigDecimal.valueOf(maxAmount).abs().compareTo(t.getOriginalAmount().abs()) < 0) {
                return false;
            }
        }

        return true;
    }

    public static class Builder {

        private Set<String> excludedAccountIds = null;
        private String excludedCategoryId = null;
        private Set<String> categoryIds = null;
        private Set<String> userIds = null;
        private Boolean requireHasMerchant = null;
        private Set<CategoryTypes> categoryTypes = null;
        private TransactionTypes type = null;
        private Period period = null;
        private Double minAmount = null;
        private Double maxAmount = null;

        private Builder copy() {
            Builder b = new Builder();
            b.excludedAccountIds = this.excludedAccountIds;
            b.excludedCategoryId = this.excludedCategoryId;
            b.requireHasMerchant = this.requireHasMerchant;
            b.userIds = this.userIds;
            b.categoryIds = this.categoryIds;
            b.categoryTypes = this.categoryTypes;
            b.type = this.type;
            b.period = this.period;
            b.minAmount = this.minAmount;
            b.maxAmount = this.maxAmount;
            return b;
        }

        public Builder requireHasMerchant() {
            Builder b = copy();
            b.requireHasMerchant = true;
            return b;
        }

        public Builder setOwnedBy(Set<String> userIds) {
            Builder b = copy();
            b.userIds = userIds;
            return b;
        }

        public Builder setCategoryType(CategoryTypes categoryType) {
            Builder b = copy();
            b.categoryTypes = Sets.newHashSet(categoryType);
            return b;
        }

        public Builder setCategoryTypes(Set<CategoryTypes> categoryTypes) {
            Builder b = copy();
            b.categoryTypes = categoryTypes;
            return b;
        }

        public Builder setCategories(List<Category> categories) {
            Builder b = copy();
            if (categories != null && !categories.isEmpty()) {
                b.categoryIds = Sets.newHashSet(Iterables.transform(categories,
                        Category::getId));
            }
            return b;
        }
        
        public Builder setExcludedCategory(List<Category> categories) {
            Builder b = copy();
            if(categories != null) {
                b.excludedCategoryId = Iterables.find(categories,
                        c -> (Objects.equal("transfers:exclude.other", c.getCode()))).getId();
            }
            return b;
        }

        public Builder setExcludedAccounts(List<Account> accounts) {
            Builder b = copy();
            if (accounts != null) {
                Iterable<Account> filtered = Iterables.filter(accounts, Account::isExcluded);

                b.excludedAccountIds = Sets.newHashSet(Iterables.transform(filtered, Account::getId));
            }
            return b;
        }
        
        public Builder setMaxAmount(double maxAmount) {
            Builder b = copy();
            b.maxAmount = maxAmount;
            return b;
        }
        
        public Builder setMinAmount(double minAmount) {
            Builder b = copy();
            b.minAmount = minAmount;
            return b;
        }
        
        public Builder setType(TransactionTypes type) {
            Builder b = copy();
            b.type = type;
            return b;
        }
        
        public Builder withinPeriod(Period period) {
            Builder b = copy();
            b.period = period;
            return b;
        }

        public CassandraTransactionFilter build() {
            CassandraTransactionFilter f = new CassandraTransactionFilter(type, categoryIds, categoryTypes,
                    excludedCategoryId, excludedAccountIds, requireHasMerchant, userIds, period, minAmount, maxAmount);
            return f;
        }
    }
}
