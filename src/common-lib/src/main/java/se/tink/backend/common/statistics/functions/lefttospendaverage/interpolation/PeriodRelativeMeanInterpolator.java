package se.tink.backend.common.statistics.functions.lefttospendaverage.interpolation;

import com.google.common.collect.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import se.tink.backend.common.statistics.functions.lefttospendaverage.dto.PeriodRelativeStatistic;

/**
 * The thought of this abstraction is to help out with transforming n number of periods with statistics data of different
 * lengths and with different number of data points into a function that for any given point in a period can return a
 * quite accurate mean value based on the given periods/data points.
 *
 * It does this by using interpolation in two passes:
 * 1. Transform each period with its data points into respective interpolation functions
 * 2. Sample from each of these interpolation functions a same-sized data set (NUMBER_OF_SAMPLES) that we calculate the
 * mean value from for each sample-percentage-point
 * 3. Use the mean values to create a new mean interpolation function
 *
 * After initializing the above with interpolateCompletePeriods,
 * use the getMean(percentageInPeriod) method to retrieve the good-enough mean value for any percentage between 0 and 1.
 *
 * NUMBER_OF_SAMPLES: How many samples the interpolation will use to translate period-by-period based relativeStatistics
 * to a mean interpolation function
 */
public class PeriodRelativeMeanInterpolator {
    private static final int NUMBER_OF_SAMPLES = 100;
    private static final double[] SAMPLING_PERCENTAGE_POINTS = createPercentagePointsForSampling();
    private PolynomialSplineFunction meanInterpolationFunction;

    private static double[] createPercentagePointsForSampling() {
        double[] percentages = new double[NUMBER_OF_SAMPLES];

        for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
            double percentage = (double)i / (double)(NUMBER_OF_SAMPLES - 1);
            percentages[i] = percentage;
        }

        return percentages;
    }

    public void interpolateCompletePeriods(Collection<Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod) {
        List<PolynomialSplineFunction> realDataInterpolators =
                createRealDataInterpolators(relativeStatisticsByPeriod);

        ValuesPercentages sampledMeanValues = sampleMeanValues(realDataInterpolators);

        this.meanInterpolationFunction = createInterpolationFunction(sampledMeanValues);
    }

    public double getMean(double percentageInPeriod) {
        Preconditions.checkState(meanInterpolationFunction != null, "loadData(statistics) was never called");
        return meanInterpolationFunction.value(percentageInPeriod);
    }

    private static PolynomialSplineFunction createInterpolationFunction(ValuesPercentages valuesPercentages) {
        return new LinearInterpolator().interpolate(valuesPercentages.getPercentages(), valuesPercentages.getValues());
    }

    private static List<PolynomialSplineFunction> createRealDataInterpolators(
            Collection<Collection<PeriodRelativeStatistic>> statisticsByPeriod) {
        List<PolynomialSplineFunction> interpolationFunctions = Lists.newArrayList();

        for (Collection<PeriodRelativeStatistic> periodStatistics : statisticsByPeriod) {
            PolynomialSplineFunction interpolateFunction = interpolateRelativeStatistics(periodStatistics);
            interpolationFunctions.add(interpolateFunction);
        }

        return interpolationFunctions;
    }

    private static PolynomialSplineFunction interpolateRelativeStatistics(
            Collection<PeriodRelativeStatistic> periodStatistics) {
        ImmutableList<PeriodRelativeStatistic> sortedStatistics = FluentIterable
                .from(periodStatistics)
                .toSortedList(Ordering.natural());

        ValuesPercentages valuesPercentages = new ValuesPercentages(sortedStatistics);

        LinearInterpolator linearInterpolator = new LinearInterpolator();
        return linearInterpolator.interpolate(
                valuesPercentages.getPercentages(),
                valuesPercentages.getValues()
        );
    }

    private static ValuesPercentages sampleMeanValues(List<PolynomialSplineFunction> polynomialSplineFunctions) {
        Preconditions.checkArgument(polynomialSplineFunctions != null && polynomialSplineFunctions.size() > 0);

        double[] values = new double[SAMPLING_PERCENTAGE_POINTS.length];

        int numberOfPeriods = polynomialSplineFunctions.size();
        for (int i = 0; i < SAMPLING_PERCENTAGE_POINTS.length; i++) {
            double percentage = SAMPLING_PERCENTAGE_POINTS[i];
            double summedValue = 0.0;

            for (PolynomialSplineFunction polynomialSplineFunction : polynomialSplineFunctions) {
                double sampledValue = polynomialSplineFunction.value(percentage);
                summedValue += sampledValue;
            }

            double mean = summedValue / (double)numberOfPeriods;
            values[i] = mean;
        }

        return new ValuesPercentages(SAMPLING_PERCENTAGE_POINTS, values);
    }

    private static class ValuesPercentages {
        private final double[] percentages;
        private final double[] values;

        private ValuesPercentages(double[] percentages, double[] values) {
            this.percentages = percentages;
            this.values = values;
        }

        private ValuesPercentages(Collection<PeriodRelativeStatistic> periodRelativeStatistics) {
            int numberOfStatistics = periodRelativeStatistics.size();
            percentages = new double[numberOfStatistics];
            values = new double[numberOfStatistics];

            int i = 0;
            for (PeriodRelativeStatistic periodRelativeStatistic : periodRelativeStatistics) {
                percentages[i] = periodRelativeStatistic.getPeriodRelativePercentage();
                values[i] = periodRelativeStatistic.getStatistic().getValue();
                i++;
            }
        }

        double[] getPercentages() {
            return percentages;
        }

        double[] getValues() {
            return values;
        }
    }
}
