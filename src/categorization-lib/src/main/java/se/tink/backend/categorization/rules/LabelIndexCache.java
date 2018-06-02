package se.tink.backend.categorization.rules;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.classifier.naivebayes.BayesUtils;
import se.tink.backend.utils.TinkCategoryMapper;
import se.tink.libraries.cluster.Cluster;

public class LabelIndexCache {

    private final static Configuration CONF = new Configuration();
    private static final String LABEL_INDEX_FILE = "labelIndex";

    private final LoadingCache<String, Map<Integer, String>> labelIndexCache;

    private LabelIndexCache(
            LoadingCache<String, Map<Integer, String>> labelIndexCache
    ) {
        this.labelIndexCache = labelIndexCache;
    }

    public Map<Integer, String> get(String resourceDirectory) throws ExecutionException {
        return labelIndexCache.get(resourceDirectory);
    }

    // TODO: Migrate this into a cluster specific Guice module.
    public static LabelIndexCache build(Cluster cluster) {
        CacheLoader<String, Map<Integer, String>> creator = new CacheLoader<String, Map<Integer, String>>() {

            @Override
            public Map<Integer, String> load(String path) throws Exception {

                Map<Integer, String> labelIndex = readIntegerStringMap(path);
                labelIndex = mapCategories(labelIndex, cluster);

                // TODO: Use ImmutableMap.
                return Collections.unmodifiableMap(labelIndex);
            }

        };

        return new LabelIndexCache(CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES).build(creator));
    }

    private static Map<Integer, String> readIntegerStringMap(String resourceDirectory) throws IOException {
        TemporaryResourcesDirectory temporaryResourceDirectory = TemporaryResourcesDirectory.create(
                "labelIndexCache",
                new File(resourceDirectory, "labelIndex").getPath(),
                new File(resourceDirectory, ".labelIndex.crc").getPath()
        );

        try {
            return BayesUtils.readLabelIndex(CONF,
                    new Path(temporaryResourceDirectory.getFile().getAbsolutePath(), LABEL_INDEX_FILE));
        } finally {
            temporaryResourceDirectory.close();
        }
    }

    private static Map<Integer, String> mapCategories(Map<Integer, String> indexToCategory, Cluster cluster) {
        Map<Integer, String> indexToMappedCategory = Maps.newHashMap();
        for (Integer key : indexToCategory.keySet()) {
            String category = indexToCategory.get(key);
            indexToMappedCategory.put(key, TinkCategoryMapper.map(category, cluster));
        }
        return indexToMappedCategory;
    }
}
