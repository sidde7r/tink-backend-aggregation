package se.tink.backend.utils;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.libraries.cluster.Cluster;

public class TinkCategoryMapper {
    private final static String SEB_CATEGORY_MAP_FILENAME = "/tink-to-seb-category-map.txt";
    private final static String KLARNA_CATEGORY_MAP_FILENAME = "/tink-to-klarna-category-map.txt";
    private final static String ELEMENT_SPLITTER = "\\s+";
    private final static LogUtils log = new LogUtils(TinkCategoryMapper.class);
    private final static Supplier<Map<String, String>> tinkToSEBCategorySupplier = Suppliers.memoizeWithExpiration(
            () -> loadMapFromFile(SEB_CATEGORY_MAP_FILENAME), 5, TimeUnit.MINUTES);
    private final static Supplier<Map<String, String>> tinkToKlarnaCategorySupplier = Suppliers.memoizeWithExpiration(
            () -> loadMapFromFile(KLARNA_CATEGORY_MAP_FILENAME), 5, TimeUnit.MINUTES);

    public static String map(String tinkCategory, Cluster cluster) {
        String mappedCategory = null;
        switch (cluster) {
        case CORNWALL:
            mappedCategory = tinkToSEBCategorySupplier.get().get(tinkCategory);
            break;
        case KIRKBY:
            mappedCategory = tinkToKlarnaCategorySupplier.get().get(tinkCategory);
            break;
        default:
            mappedCategory = tinkCategory;
        }
        return mappedCategory;
    }

    private static Map<String, String> loadMapFromFile(String filename) {
        try (InputStream resource = TinkCategoryMapper.class.getResourceAsStream(filename)) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .map(line -> line.split(ELEMENT_SPLITTER))
                    .filter(pieces -> {
                        if (pieces.length != 2) {
                            log.warn("Incorrect number of pieces: " + pieces.length);
                        }
                        return pieces.length == 2;
                    })
                    .collect(Collectors.toMap(pieces -> pieces[0], pieces -> pieces[1]));
        } catch (IOException e) {
            return ImmutableMap.of();
        }
    }
}
