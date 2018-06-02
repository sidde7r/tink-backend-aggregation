package se.tink.backend.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class MortgageDistribution {
    private static final LogUtils log = new LogUtils(MortgageDistribution.class);

    public static BigDecimal AVERAGE_RATE = new BigDecimal("0.018");
    public static BigDecimal LOW_RATE = new BigDecimal("0.015");

    private static final String DEFAULT_INPUT_FILENAME = "data/seeding/mortgage_distribution.csv";
    private static final double DEFAULT_BUCKET_SIZE = 0.001;
    private final List<Bucket> buckets;

    public MortgageDistribution(List<Bucket> buckets) {
        this.buckets = buckets;
    }

    public static MortgageDistribution loadDefault() throws IOException {
        return load(DEFAULT_INPUT_FILENAME, DEFAULT_BUCKET_SIZE);
    }

    public static MortgageDistribution load(String filename, double bucketSize) throws IOException {
        return new MortgageDistribution(loadBucketsFromFile(filename, bucketSize));
    }

    private static List<Bucket> loadBucketsFromFile(String filename, double bucketSize) throws IOException {
        BigDecimal step = BigDecimal.valueOf(bucketSize);
        List<Bucket> buckets;
        try (Stream<String> lines = Files.lines(Paths.get(filename), Charset.defaultCharset())) {
            buckets = lines.skip(1) // header
                    .map(StringUtils::parseCSV)
                    .peek(line -> {
                        if (line.size() != 2) {
                            log.warn(String.format("Unexpected line: (%s). Expecting (bucket, distribution)",
                                    StringUtils.join(line, ",", "")));
                        }
                    })
                    .filter(line -> line.size() >= 2)
                    .map(line -> line.stream()
                            .map(BigDecimal::new)
                            .collect(Collectors.toList()))
                    .map(line -> new Bucket(
                            line.get(0).compareTo(step) < 0 ? BigDecimal.ZERO : line.get(0).subtract(step),
                            line.get(0), line.get(1)))
                    .collect(Collectors.toList());
        }

        return fillGaps(buckets, step, BigDecimal.ZERO);
    }

    @VisibleForTesting
    static List<Bucket> fillGaps(List<Bucket> buckets, BigDecimal bucketSize, BigDecimal defaultValue) {
        if (buckets.isEmpty()) {
            return buckets;
        }
        BigDecimal expectedUpperEndpoint = Iterables.getLast(buckets).getRange().lowerEndpoint();
        for (int i = buckets.size() - 2; i >= 0; i--) {
            while (buckets.get(i).getRange().upperEndpoint().compareTo(expectedUpperEndpoint) != 0) {
                BigDecimal lowerEndpoint = expectedUpperEndpoint.subtract(bucketSize);
                buckets.add(i + 1, new Bucket(lowerEndpoint, expectedUpperEndpoint, defaultValue));
                expectedUpperEndpoint = lowerEndpoint;
            }
            expectedUpperEndpoint = buckets.get(i).getRange().lowerEndpoint();
        }

        return buckets;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public static class Bucket {
        private Range<BigDecimal> range;
        private BigDecimal value;

        Bucket(BigDecimal lowerEndpoint, BigDecimal upperEndpoint, BigDecimal value) {
            this(Range.closedOpen(lowerEndpoint, upperEndpoint), value);
        }

        Bucket(Range<BigDecimal> range, BigDecimal value) {
            this.range = range;
            this.value = value;
        }

        public Range<BigDecimal> getRange() {
            return range;
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
