package se.tink.backend.categorization.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.name.Named;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.rules.FastTextClassifier;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.FastTextConfiguration;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Market;

public class FastTextProcessorCategorizerFactory {
    private static final LogUtils log = new LogUtils(FastTextProcessorCategorizerFactory.class);
    static final Pattern urlPattern = Pattern
            .compile("(?<protocol>[a-z0-9]+)://(?<host>[^/]*)/(?<path>.+)");

    private final int topLabels;
    private final CategoryConfiguration categoryConfiguration;
    private final CategorizationConfiguration categorizationConfiguration;
    private final ClusterCategories categories;
    private final List<FastTextClassifier> constructions = Lists.newArrayList();
    private final File executable;
    private final List<FastTextConfiguration> models;

    private ImmutableMap<Market.Code, Supplier<File>> filesByMarket;

    @Inject
    public FastTextProcessorCategorizerFactory(int topLabels, File executable,
            @Named("fastTextExpenseConfiguration") List<FastTextConfiguration> models, CategoryConfiguration categoryConfiguration,
            CategorizationConfiguration categorizationConfiguration, ClusterCategories categories) {
        this.categoryConfiguration = categoryConfiguration;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categories = categories;
        this.topLabels = topLabels;
        this.executable = executable;
        this.models = models;

        ImmutableMap.Builder<Market.Code, Supplier<File>> filesBuilder = ImmutableMap.builder();
        models.forEach(m -> filesBuilder
                .put(m.getMarket(), Suppliers.memoize(() -> this.getFileRuntimeException(m.getModel()))));
        filesByMarket = filesBuilder.build();
    }

    public Map<Market.Code, FastTextClassifier> build() {
        Map<Market.Code, FastTextClassifier> classifiers = Maps.newHashMap();

        for (FastTextConfiguration model : models) {
            File modelFile = filesByMarket.get(model.getMarket()).get();
            FastTextClassifier construction = new FastTextClassifier(executable, modelFile, categoryConfiguration,
                    categorizationConfiguration, topLabels, categories,
                    model.getPreformatters().stream().map(p -> p.build()).collect(Collectors.toList()));
            construction.start();
            synchronized (constructions) {
                constructions.add(construction);
            }

            classifiers.put(model.getMarket(), construction);
        }

        return classifiers;
    }

    private File getFileRuntimeException(String modelUrl) {
        try {
            return getFile(modelUrl);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String modelUrl) throws IOException, InterruptedException {

        Matcher modelURL = urlPattern.matcher(modelUrl);
        if (!modelURL.matches()) {
            throw new IllegalArgumentException("Could not parse URL: " + modelUrl);
        }

        File categoryModel;
        switch (modelURL.group("protocol")) {
        case "resource":
            categoryModel = File.createTempFile("categoryModel", ".bin");
            log.info("Using temporary file for fastText modelUrl: " + categoryModel.getAbsolutePath());
            categoryModel.deleteOnExit();

            String path = "/" + modelURL.group("path");
            log.info("Resource fasttext modelUrl: " + path);
            InputStream stream = this.getClass().getResourceAsStream(path);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(categoryModel));
            ByteStreams.copy(stream, out);
            stream.close();
            out.close();

            break;
        case "file":
            categoryModel = new File("/" + modelURL.group("path"));
            Preconditions.checkArgument(categoryModel.exists());
            break;
        default:
            throw new IllegalStateException("Unrecognized protocol: " + modelURL.group("protocol"));
        }

        return categoryModel;
    }

    public void stop() {
        synchronized (constructions) {
            for (FastTextClassifier construction : constructions) {
                construction.stop();
            }
        }
    }
}
