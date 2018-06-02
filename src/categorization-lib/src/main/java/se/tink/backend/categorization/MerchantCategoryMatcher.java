package se.tink.backend.categorization;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.categorization.abnamro.AbnAmroMerchantDescription;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

/**
 * Matcher that match a merchant description or merchant category code (mcc) to a category vector.
 * <p>
 * - Different files for different clusters since each cluster have their own set of categories.
 * - ABN AMRO is also using a set of predefined description mappings for those descriptions that aren't covered by
 * the mcc mapping files.
 */
public class MerchantCategoryMatcher {

    private final static LogUtils log = new LogUtils(MerchantCategoryMatcher.class);

    private final static String DEFAULT_MCC_MAPPING_FILEPATH = "/mcc-probabilistic-mapping.txt";
    private final static String ABN_AMRO_MCC_MAPPING_FILEPATH = "/abnamro/mcc-probabilistic-mapping.txt";
    private static final Splitter TAB_SPLITTER = Splitter.on('\t').trimResults();

    private final ImmutableMap<Integer, CategorizationVector> categorizationVectorByMerchantCode;
    private final ImmutableMap<String, CategorizationVector> categorizationVectorByMerchantDescription;

    private MerchantCategoryMatcher(ImmutableMap<Integer, CategorizationVector> categorizationVectorByMerchantCode,
            ImmutableMap<String, CategorizationVector> categorizationVectorByMerchantDescription) {

        this.categorizationVectorByMerchantCode = categorizationVectorByMerchantCode;
        this.categorizationVectorByMerchantDescription = categorizationVectorByMerchantDescription;
    }

    public CategorizationVector findByDescription(String merchantDescription) {
        if (Strings.isNullOrEmpty(merchantDescription)) {
            return null;
        }

        return categorizationVectorByMerchantDescription.get(merchantDescription.toUpperCase());
    }

    public CategorizationVector findByCode(int code) {
        return categorizationVectorByMerchantCode.get(code);
    }

    public static Builder builder(Cluster cluster) {
        return new Builder(cluster);
    }

    public static class Builder {

        private Cluster cluster;

        private Builder(Cluster cluster) {
            this.cluster = cluster;
        }

        public MerchantCategoryMatcher build() throws IOException {

            Map<Integer, CategorizationVector> categorizationVectorsByCode = Maps.newHashMap();
            Map<String, CategorizationVector> categorizationVectorsByDescription = Maps.newHashMap();

            String filePath;

            if (Objects.equals(Cluster.ABNAMRO, cluster)) {
                filePath = ABN_AMRO_MCC_MAPPING_FILEPATH;
            } else {
                filePath = DEFAULT_MCC_MAPPING_FILEPATH;
            }

            List<List<String>> lines;
            try (InputStream resource = getClass().getResourceAsStream(filePath)) {
                lines = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                        .lines()
                        .map(line -> ImmutableList.copyOf(TAB_SPLITTER.split(line)))
                        .collect(Collectors.toList());
            }

            // Line format is "Merchant Code\tCategorization Vector(s)\tOne or multiple descriptions"
            for (List<String> line : lines) {

                if (line.size() < 2) {
                    log.warn("Wrong number of columns.");
                    continue;
                }

                int merchantCode = Integer.parseInt(line.get(0));
                String categorizationVector = line.get(1);

                if (categorizationVector.isEmpty()) {
                    continue;
                }

                CategorizationVector probabilisticMapping = new CategorizationVector();

                for (Map.Entry<String, String> pair : StringUtils.parseCSKVPairs(categorizationVector)) {
                    probabilisticMapping.setDistribution(pair.getKey(), Double.parseDouble(pair.getValue()));
                }

                categorizationVectorsByCode.put(merchantCode, probabilisticMapping);

                // Add all different versions of merchant descriptions
                for (int i = 2; i < line.size(); i++) {
                    String description = StringUtils.trim(line.get(i));
                    if (!description.isEmpty()) {
                        categorizationVectorsByDescription.put(description.toUpperCase(), probabilisticMapping);
                    }
                }
            }

            // Add extra mappings that isn't covered by the the merchant category code files
            if (Objects.equals(Cluster.ABNAMRO, cluster)) {
                categorizationVectorsByDescription.putAll(AbnAmroMerchantDescription.CATEGORIZATION_VECTORS);
            }

            return new MerchantCategoryMatcher(ImmutableMap.copyOf(categorizationVectorsByCode),
                    ImmutableMap.copyOf(categorizationVectorsByDescription));
        }
    }
}
