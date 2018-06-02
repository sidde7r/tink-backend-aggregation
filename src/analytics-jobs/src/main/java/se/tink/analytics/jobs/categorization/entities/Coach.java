package se.tink.analytics.jobs.categorization.entities;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.classifier.naivebayes.training.TrainNaiveBayesJob;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.CategorizationUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.QuickIndex;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class Coach {

    private final static String DEFAULT_BASE_DIR = "data/categorization/naivebayes/";
    private final static String DESCRIPTION_INDEX = "/descriptionIndex";
    private final static String LABEL_INDEX = "/labelIndex";

    private final static String MODEL_DIR = "/model/";
    private final static String MODEL_BIN = MODEL_DIR + "naiveBayesModel.bin";
    private final static String SEQUENCED_TRANSACTIONS = "/transactions.seq";
    private final static String TARGET_DIR = "/new/%s/";
    private final static String TEMP_DIR = "/temp/";
    private final static String TRAINING_DIR = "/training/";

    private final static LogUtils log = new LogUtils(Coach.class);

    private final static float DEFAULT_ALPHA = 1;
    private final static float DEFAULT_BOOST = 5;
    private final static int DEFAULT_N = 3;

    private final String categoryType;
    private final String market;
    private final String key;
    private final Set<String> unhandledCategories;
    private final String workingDirectory;

    private final int n;
    private final float boost;

    private SequenceFile.Writer writer;

    private QuickIndex descriptionIndex = new QuickIndex(1000000);

    private Configuration conf = new Configuration();

    public Coach(String market, String categoryType, Set<String> unhandledCategories, String workingDirectory) {
        this.categoryType = categoryType.toLowerCase();
        this.market = market.toLowerCase();
        this.key = createKey(market, categoryType);
        this.n = DEFAULT_N;
        this.boost = DEFAULT_BOOST;
        this.unhandledCategories = unhandledCategories;
        this.workingDirectory = Strings.isNullOrEmpty(workingDirectory) ? DEFAULT_BASE_DIR : workingDirectory;
    }

    public static String createKey(String market, String categoryType) {
        return String.format("%s-%s", categoryType.toLowerCase(), market.toLowerCase());
    }

    public String getKey() {
        return key;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public String getMarket() {
        return market;
    }

    public void cleanup() {

        System.out.println(String.format("# CLEAN UP (%s)", getKey()));

        Path trainingPath = getTrainingPath();
        Path trainingDescriptionIndex = trainingPath.suffix(DESCRIPTION_INDEX);
        Path trainingLabelIndex = trainingPath.suffix(TEMP_DIR).suffix(LABEL_INDEX);
        Path trainingModelBin = trainingPath.suffix(MODEL_BIN);

        Path targetPath = getTargetPath();
        Path targetDescriptionIndex = targetPath.suffix(DESCRIPTION_INDEX);
        Path targetLabelIndex = targetPath.suffix(LABEL_INDEX);
        Path targetModelBin = targetPath.suffix(MODEL_BIN);

        try {
            FileSystem fs = FileSystem.getLocal(conf);

            try {
                fs.copyFromLocalFile(trainingDescriptionIndex, targetDescriptionIndex);
                fs.copyFromLocalFile(trainingLabelIndex, targetLabelIndex);
                fs.copyFromLocalFile(trainingModelBin, targetModelBin);
            } catch (IOException e) {
                // Do nothing.
            }

            fs.delete(trainingPath, true);
        } catch (IOException e) {
            // Do nothing.
        }
    }

    public void preparePreprocessing() throws IOException {

        Path trainingPath = getTrainingPath();
        Path sequencedTransactionsPath = trainingPath.suffix(SEQUENCED_TRANSACTIONS);

        FileSystem fs = FileSystem.getLocal(conf);
        fs.delete(sequencedTransactionsPath, false);

        this.writer = new SequenceFile.Writer(fs, conf, sequencedTransactionsPath, Text.class,
                VectorWritable.class);
    }

    public void preprocess(String description, String categoryCode) throws IOException {
        if (unhandledCategories.contains(categoryCode)) {
            return;
        }

        descriptionIndex.add(description);

        Vector vector = CategorizationUtils.createFeatureVector(description, n, boost, descriptionIndex);

        writer.append(new Text("/" + categoryCode + "/"), new VectorWritable(vector));
    }

    public void finalizePreprocessing() throws IOException {
        // Save description index to drive (as binary JSON)
        Path trainingPath = getTrainingPath();
        SerializationUtils.serializeToBinary(new File(trainingPath.suffix(DESCRIPTION_INDEX).toString()),
                descriptionIndex);

        writer.close();
    }

    public void train() {
        train(DEFAULT_ALPHA);
    }

    public void train(float alpha) {

        System.out.println(String.format("# TRAIN (%s)", getKey()));

        Stopwatch stopwatch = Stopwatch.createStarted();

        Path trainingPath = getTrainingPath();
        Path sequencedTransactionsPath = trainingPath.suffix(SEQUENCED_TRANSACTIONS);
        Path modelPath = trainingPath.suffix(MODEL_DIR);
        Path tempPath = trainingPath.suffix(TEMP_DIR);

        try {
            FileSystem fs = FileSystem.getLocal(conf);
            fs.delete(modelPath, true);
            fs.delete(tempPath, true);

            TrainNaiveBayesJob trainNaiveBayes = new TrainNaiveBayesJob();
            trainNaiveBayes.setConf(conf);
            trainNaiveBayes.run(new String[] {
                    "--alphaI", Float.toString(alpha),
                    "--input", sequencedTransactionsPath.toString(),
                    "--output", modelPath.toString(),
                    "--tempDir", tempPath.toString(),
                    "--extractLabels",
                    "--overwrite"
            });
        } catch (Exception e) {
            log.warn("Unable to train model.", e);
        }

        System.out.println(String.format("Training took %ss", stopwatch.elapsed(TimeUnit.SECONDS)));
        System.out.println(String.format("\tUnique cleaned descriptions: %s (of maximum %s)", descriptionIndex.size(),
                descriptionIndex.getMaxSize()));
    }

    private Path getTargetPath() {
        String targetDir = String.format(TARGET_DIR, ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));
        return new Path(workingDirectory).suffix(targetDir + key);
    }

    private Path getTrainingPath() {
        return new Path(workingDirectory).suffix(TRAINING_DIR + key);
    }
}
