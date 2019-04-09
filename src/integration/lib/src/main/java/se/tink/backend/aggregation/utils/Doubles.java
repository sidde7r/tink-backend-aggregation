package se.tink.backend.aggregation.utils;

import com.google.common.math.DoubleMath;

public class Doubles {

    /**
     * Like {@link DoubleMath#fuzzyEquals(double, double, double)}, but supports nullables.
     *
     * @param a first nullable element
     * @param b second nullable element
     * @param tolerance absolute difference tolerance. See {@link DoubleMath#fuzzyEquals(double,
     *     double, double)} for details.
     */
    public static boolean fuzzyEquals(Double a, Double b, double tolerance) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b != null) {
            return DoubleMath.fuzzyEquals(a, b, tolerance);
        } else /* (a != null ^ b != null) */ {
            return false;
        }
    }
}
