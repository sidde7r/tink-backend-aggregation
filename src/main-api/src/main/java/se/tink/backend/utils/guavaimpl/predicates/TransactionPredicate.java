package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Transaction;

public class TransactionPredicate implements Predicate<Transaction> {

    private final String categoryId;
    private final String merchantId;
    private final Period period;
    private final Boolean requireHasMerchant;
    private final CategoryTypes type;

    private TransactionPredicate(
            final String categoryId,
            final String merchantId,
            final Period period,
            final Boolean requireHasMerchant,
            final CategoryTypes type) {

        this.categoryId = categoryId;
        this.merchantId = merchantId;
        this.period = period;
        this.requireHasMerchant = requireHasMerchant;
        this.type = type;
    }

    @Override
    public boolean apply(Transaction t) {
        if (requireHasMerchant && t.getMerchantId() == null) {
            return false;
        }

        if (period != null) {
            if (!t.getDate().after(period.getStartDate())) {
                return false;
            }
            if (!t.getDate().before(period.getEndDate())) {
                return false;
            }
        }

        if (type != null && !Objects.equal(t.getCategoryType(), type)) {
            return false;
        }

        if (categoryId != null && !Objects.equal(categoryId, t.getCategoryId())) {
            return false;
        }

        if (merchantId != null && !Objects.equal(merchantId, t.getMerchantId())) {
            return false;
        }

        return true;
    }

    public static class Builder {

        private String categoryId = null;
        private String merchantId = null;
        private Period period = null;
        private Boolean requireHasMerchant = null;
        private CategoryTypes type = null;

        private Builder copy() {
            Builder b = new Builder();
            b.categoryId = this.categoryId;
            b.merchantId = this.merchantId;
            b.period = this.period;
            b.requireHasMerchant = this.requireHasMerchant;
            b.type = this.type;
            return b;
        }

        public Builder requireHasMerchant() {
            Builder b = copy();
            b.requireHasMerchant = true;
            return b;
        }

        public Builder setCategoryType(CategoryTypes type) {
            Builder b = copy();
            b.type = type;
            return b;
        }

        public Builder categorizedTo(String categoryId) {
            Builder b = copy();
            b.categoryId = categoryId;
            return b;
        }

        public Builder merchantizedTo(String merchantId) {
            Builder b = copy();
            b.merchantId = merchantId;
            return b;
        }

        public Builder withinPeriod(Period period) {
            Builder b = copy();
            b.period = period;
            return b;
        }

        public TransactionPredicate build() {
            TransactionPredicate f = new TransactionPredicate(categoryId, merchantId, period, requireHasMerchant, type);
            return f;
        }
    }
}
