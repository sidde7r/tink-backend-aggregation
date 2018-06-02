package se.tink.backend.main.histograms;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class SavingDistribution {
    private static final LogUtils log = new LogUtils(SavingDistribution.class);

    public static final int AVERAGE_SAVING_AMOUNT = 42000;
    public static final int AVERAGE_SAVING_AND_INVESTMENT_AMOUNT = 83000;

    private static final String DEFAULT_INPUT_FILENAME = "data/seeding/savings-and-investments_distribution.csv";
    private static final Comparator<Bucket> BUCKET_COMPARATOR = Comparator.comparing(Bucket::getAmountRange,
            Comparator.comparing(Range::lowerEndpoint));

    private final List<Bucket> buckets;

    public SavingDistribution(List<Bucket> buckets) {
        this.buckets = buckets;
        this.buckets.sort(BUCKET_COMPARATOR);
    }

    public static SavingDistribution loadDefault() throws IOException {
        return load(DEFAULT_INPUT_FILENAME);
    }

    public static SavingDistribution load(String filename) throws IOException {
        return new SavingDistribution(loadBucketsFromFile(filename));
    }

    private static List<Bucket> loadBucketsFromFile(String filename) throws IOException {
        List<List<BigDecimal>> table = Files.readAllLines(Paths.get(filename)).stream()
                .skip(1) // header
                .map(StringUtils::parseCSV)
                .peek(line -> {
                    if (line.size() != 3) {
                        log.warn(String.format("Unexpected line: (%s). Expecting (bucket, frequency, percentile)",
                                StringUtils.join(line, ",", "")));
                    }
                })
                .filter(line -> line.size() >= 3)
                .map(line -> line.stream()
                        .map(BigDecimal::new)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return createBuckets(table);
    }

    private static List<Bucket> createBuckets(List<List<BigDecimal>> table) {
        if (table.isEmpty()) {
            return Collections.emptyList();
        }

        List<Bucket> buckets = Lists.newArrayList();

        for (int i = 0; i < table.size() - 1; i++) {
            List<BigDecimal> line = table.get(i);
            List<BigDecimal> nextLine = table.get(i + 1);
            buckets.add(new Bucket(line.get(0), nextLine.get(0), line.get(1), line.get(2)));
        }

        List<BigDecimal> lastLine = table.get(table.size() - 1);
        buckets.add(new Bucket(Range.atLeast(lastLine.get(0)), lastLine.get(1), lastLine.get(2)));

        return buckets;
    }

    public Optional<Bucket> findBucket(double amount) {
        return findBucket(BigDecimal.valueOf(amount));
    }

    public Optional<Bucket> findBucket(BigDecimal amount) {
        return binarySearch(amount);
    }

    @VisibleForTesting
    Optional<Bucket> binarySearch(BigDecimal amount) {
        int lowIndex = 0;
        int highIndex = buckets.size() - 1;

        while (highIndex >= lowIndex) {
            int middleIndex = (lowIndex + highIndex) / 2;
            if (buckets.get(middleIndex).getAmountRange().contains(amount)) {
                return Optional.ofNullable(buckets.get(middleIndex));
            }

            if (buckets.get(middleIndex).getAmountRange().lowerEndpoint().compareTo(amount) > 0) {
                highIndex = middleIndex - 1;
            } else {
                lowIndex = middleIndex + 1;
            }
        }

        return Optional.empty();
    }

    public static class Bucket {
        private Range<BigDecimal> amountRange;
        private BigDecimal frequency;
        private BigDecimal percentile;

        Bucket(BigDecimal lowerEndpoint, BigDecimal upperEndpoint, BigDecimal frequency,
                BigDecimal percentile) {
            this(Range.closedOpen(lowerEndpoint, upperEndpoint), frequency, percentile);
        }

        Bucket(Range<BigDecimal> amountRange, BigDecimal frequency, BigDecimal percentile) {
            this.amountRange = amountRange;
            this.frequency = frequency;
            this.percentile = percentile;
        }

        public Range<BigDecimal> getAmountRange() {
            return amountRange;
        }

        public BigDecimal getFrequency() {
            return frequency;
        }

        public BigDecimal getPercentile() {
            return percentile;
        }
    }
}
