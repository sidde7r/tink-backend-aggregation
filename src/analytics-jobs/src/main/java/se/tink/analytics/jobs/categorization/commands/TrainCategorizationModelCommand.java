package se.tink.analytics.jobs.categorization.commands;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.tink.analytics.jobs.categorization.entities.Coach;
import se.tink.analytics.jobs.categorization.utils.CoachesUtils;

public class TrainCategorizationModelCommand {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    public static void main(String[] args) throws Exception {

        TrainingContext trainingContext = parseArguments(args);

        System.out.println(String.format("Using context: %s", trainingContext));

        new TrainCategorizationModelCommand().train(trainingContext);
    }

    private void train(TrainingContext context) throws Exception {
        System.out.println("Starting training!");

        Stopwatch stopwatch = Stopwatch.createStarted();

        final Map<String, Coach> coachByKey = CoachesUtils.getCoaches(context.markets, context.categoryTypes,
                context.unhandledCategories, context.workingDirectory);

        train(coachByKey);

        System.out.println(String.format("Training took %ss", stopwatch.elapsed(TimeUnit.SECONDS)));
        System.out.println("Training completed!");
    }

    private void train(Map<String, Coach> coachByKey) {
        for (Coach coach : coachByKey.values()) {
            coach.train();
            coach.cleanup();
        }
    }

    private static class TrainingContext {

        public ImmutableSet<String> markets;
        public ImmutableSet<String> categoryTypes;
        public Set<String> unhandledCategories;
        public String workingDirectory;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("markets", markets)
                    .add("categoryTypes", categoryTypes)
                    .toString();
        }
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static TrainingContext parseArguments(String args[]) throws ParseException {

        final String market = "market";
        final String markets = "markets";
        final String categoryType = "categoryType";
        final String categoryTypes = "categoryTypes";
        final String workingDirectory = "workingDirectory";
        final String unhandledCategory = "unhandledCategory";
        final String unhandledCategories = "unhandledCategories";

        Options options = new Options();

        options.addOption("w", workingDirectory, true, "Working directory");

        OptionGroup marketOptionGroup = new OptionGroup();
        marketOptionGroup.addOption(OptionBuilder.withLongOpt(market).hasArg().create(market));
        marketOptionGroup.addOption(OptionBuilder.withLongOpt(markets).hasArgs().create(markets));
        marketOptionGroup.setRequired(true);

        OptionGroup categoryOptionGroup = new OptionGroup();
        categoryOptionGroup.addOption(OptionBuilder.withLongOpt(categoryType).hasArg().create(categoryType));
        categoryOptionGroup.addOption(OptionBuilder.withLongOpt(categoryTypes).hasArg().create(categoryTypes));
        categoryOptionGroup.setRequired(true);

        OptionGroup unhandledCategoriesOptionGroup = new OptionGroup();
        unhandledCategoriesOptionGroup.addOption(OptionBuilder.withLongOpt(categoryType).hasArg().create(unhandledCategories));
        unhandledCategoriesOptionGroup.addOption(OptionBuilder.withLongOpt(categoryType).hasArg().create(unhandledCategory));
        unhandledCategoriesOptionGroup.setRequired(true);

        options.addOptionGroup(marketOptionGroup);
        options.addOptionGroup(categoryOptionGroup);
        options.addOptionGroup(unhandledCategoriesOptionGroup);

        CommandLine line = new BasicParser().parse(options, args, true);

        TrainingContext context = new TrainingContext();

        String inputMarkets = line.getOptionValue(marketOptionGroup.getSelected()).toLowerCase();
        String inputCategoryTypes = line.getOptionValue(categoryOptionGroup.getSelected()).toLowerCase();
        String inputUnhandledCategories = line.getOptionValue(unhandledCategoriesOptionGroup.getSelected())
                .toLowerCase();

        context.markets = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputMarkets));
        context.categoryTypes = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputCategoryTypes));
        context.unhandledCategories = ImmutableSet.copyOf(COMMA_SPLITTER.splitToList(inputUnhandledCategories));
        context.workingDirectory = line.getOptionValue(workingDirectory);

        return context;
    }

}
