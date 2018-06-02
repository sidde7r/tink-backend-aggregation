package se.tink.backend.common.utils.nullables;

import java.util.Comparator;

import javax.annotation.Nullable;

import se.tink.backend.common.utils.nullables.Nullables.NullComparableFallback;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Wrapper of Guava's {@link com.google.common.collect.ComparisonChain} that supports ordering by <code>null</code> through
 * {@link #compareNullables(Object, Object)}.
 * 
 * @see ComparisonChain for real documentation.
 */
public class NullableComparisonChain {
    public static NullableComparisonChain start() {
        return new NullableComparisonChain(ComparisonChain.start());
    }

    private ComparisonChain delegate;

    private NullableComparisonChain() {
    }

    private NullableComparisonChain(ComparisonChain delegate) {
        this.delegate = delegate;
    }

    /**
     * Old name of {@link #compareFalseFirst}.
     * 
     * @deprecated Use {@link #compareFalseFirst}; or, if the parameters passed are being either negated or reversed,
     *             undo the negation or reversal and use {@link #compareTrueFirst}. <b>This method is scheduled for
     *             deletion in September 2013.</b>
     */
    @Deprecated
    public final NullableComparisonChain compare(boolean left, boolean right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two comparable objects as specified by {@link Comparable#compareTo}, <i>if</i> the result of this
     * comparison chain has not already been determined.
     */
    public NullableComparisonChain compare(Comparable<?> left, Comparable<?> right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two {@code double} values as specified by {@link Double#compare}, <i>if</i> the result of this
     * comparison chain has not already been determined.
     */
    public NullableComparisonChain compare(double left, double right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two {@code float} values as specified by {@link Float#compare}, <i>if</i> the result of this comparison
     * chain has not already been determined.
     */
    public NullableComparisonChain compare(float left, float right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two {@code int} values as specified by {@link Ints#compare}, <i>if</i> the result of this comparison
     * chain has not already been determined.
     */
    public NullableComparisonChain compare(int left, int right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two {@code long} values as specified by {@link Longs#compare}, <i>if</i> the result of this comparison
     * chain has not already been determined.
     */
    public NullableComparisonChain compare(long left, long right) {
        delegate = delegate.compare(left, right);
        return this;
    }

    /**
     * Compares two objects using a comparator, <i>if</i> the result of this comparison chain has not already been
     * determined.
     */
    public <T> NullableComparisonChain compare(@Nullable T left, @Nullable T right, Comparator<T> comparator) {
        delegate = delegate.compare(left, right, comparator);
        return this;
    }

    /**
     * Compares two {@code boolean} values, considering {@code false} to be less than {@code true}, <i>if</i> the result
     * of this comparison chain has not already been determined.
     * 
     * @since 12.0 (present as {@code compare} since 2.0)
     */
    public NullableComparisonChain compareFalseFirst(boolean left, boolean right) {
        delegate = delegate.compareFalseFirst(left, right);
        return this;
    }

    /**
     * Checks that left and right are not null. If they are, {@link NullComparableFallback} is thrown which can be used
     * to order by null values.
     * 
     * @throws NullComparableFallback
     */
    public NullableComparisonChain compareNullables(Object left, Object right) throws NullComparableFallback {
        Nullables.orderByNull(left, right);
        return this;
    }

    /**
     * Compares two {@code boolean} values, considering {@code true} to be less than {@code false}, <i>if</i> the result
     * of this comparison chain has not already been determined.
     * 
     * @since 12.0
     */
    public NullableComparisonChain compareTrueFirst(boolean left, boolean right) {
        delegate = delegate.compareTrueFirst(left, right);
        return this;
    }

    /**
     * Ends this comparison chain and returns its result: a value having the same sign as the first nonzero comparison
     * result in the chain, or zero if every result was zero.
     */
    public int result() {
        return delegate.result();
    }
}