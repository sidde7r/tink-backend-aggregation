package se.tink.backend.common.utils.nullables;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import javax.annotation.Nullable;

/**
 * Helper class to deal with nullables.
 */
public class Nullables {

    public static class NullablePair {
        private boolean in2;
        private boolean leftIsNull;
        private Object left;
        private Object right;

        protected NullablePair(@Nullable Object left, @Nullable Object right) {
            this.leftIsNull = left == null;
            this.in2 = right == null;

            // Only used by #toString().
            this.left = left;
            this.right = right;
        }
        
        public Object getLeft() {
            return left;
        }
        
        public Object getRight() {
            return right;
        }

        public boolean anyNull() {
            return leftIsNull || in2;
        }

        public int compare() {
            return NULL_COMPARATOR.compare(left, right);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("o1", left).add("o2", right).toString();
        }
    }

    public static class NullComparator extends Ordering<Object> {

        @Override
        public int compare(@Nullable Object left, @Nullable Object right) {
            boolean leftIsNull = left == null;
            boolean rightIsNull = right == null;

            if (leftIsNull == rightIsNull) {
                return 0;
            } else if (leftIsNull) {
                return -1;
            } else /* (rightIsNull) */{
                return 1;
            }
        }
    }

    public static class NullComparableFallback extends Exception {
        private static final long serialVersionUID = -8391146595510506783L;
        private NullablePair pair;

        public NullComparableFallback(NullablePair pair) {
            this.pair = pair;
        }

        /**
         * Compare the null values. Null values are first (<0).
         * 
         * Same as calling <code>comparisonFallback.getPair().compare()</code>.
         * 
         * @return order of the two null values
         */
        public int compare() {
            return pair.compare();
        }

        /**
         * Get the actual values.
         * @return
         */
        public NullablePair getPair() {
            return pair;
        }
    }

    private final static NullComparator NULL_COMPARATOR = new NullComparator();

    /**
     * Helper method to quickly do nested null checks in Comparable and Comparators implementations.
     * 
     * Example: <code>
     * try {
     *     chain.orderByNull(myObject1.getProperty(), myObject2.getProperty());
     *     chain.orderByNull(myObject1.getProperty().getSubProperty(), myObject2.getProperty().getSubProperty());
     * } except (Nullables.NullComparisonFallback nullFallback) {
     *     return nullFallback.compare();
     * }
     * 
     * return ComparisonChain.start().compare(myObject1.getProperty().getSubProperty().value(),
     *     myObject2.getProperty().getSubProperty().value());
     * </code>
     * 
     * @param left nullable object
     * @param right nullable object
     * @throws NullComparableFallback if the any of the objects are null.
     */
    static void orderByNull(@Nullable Object left, @Nullable Object 
            right) throws NullComparableFallback {
        NullablePair pair = pair(left, right);
        if (pair.anyNull()) {
            throw new NullComparableFallback(pair);
        }
    }

    /**
     * Create a helper class to quickly do nested null checks in Comparable and Comparators implementations.
     * 
     * Example: <code>
     * NullablePair pair;
     * 
     * pair = Nullables.pair(myObject1.getProperty(), myObject2.getProperty());
     * if (pair.anyNull())
     *     return pair.compare();
     *     
     * pair = Nullables.pair(myObject1.getProperty().getSubProperty(), myObject2.getProperty().getSubProperty());
     * if (pair.anyNull())
     *     return pair.compare();
     *     
     * return ComparisonChain.start().compare(myObject1.getProperty().getSubProperty().value(),
     *      myObject2.getProperty().getSubProperty().value());
     * </code>
     * 
     * @param o1
     *            the first object
     * @param o2
     *            the second object
     * @return an Ordering/Comparator that can be used to order by nullability.
     */
    // Currently private because I found orderByNull to be a nicer abstraction.
    private static NullablePair pair(@Nullable Object o1, @Nullable Object o2) {
        return new NullablePair(o1, o2);
    }
}
