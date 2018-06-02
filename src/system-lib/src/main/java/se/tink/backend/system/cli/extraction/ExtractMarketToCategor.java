package se.tink.backend.system.cli.extraction;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.inject.Injector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.http.client.WebResourceFactory;

public class ExtractMarketToCategor extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ExtractMarketToCategor.class);
    public static final Splitter PERIOD_SPLITTER = Splitter.on(".");

    private CategoryRepository categoryRepository;

    public ExtractMarketToCategor() {
        super("extract-market-to-categor",
                "Extract common transaction categories to categor.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.categoryRepository = serviceContext.getRepository(CategoryRepository.class);

        final String url = getRequiredSystemProperty("url");
        final String apiToken = getRequiredSystemProperty("apiToken");
        final String marketName = getRequiredSystemProperty("marketName");
        final String categoryCodePrefix = System.getProperty("categoryCodePrefix", "");

        // Number of unique users we want to categorize the description.
        final int marketGoalAnswers = Integer.parseInt(getRequiredSystemProperty("marketGoalAnswers"));

        Client client = Client.create();
        WebResource jerseyResource = client.resource(url);
        TrainingSetService categorService = WebResourceFactory.newResource(TrainingSetService.class, jerseyResource);

        TrainingSet trainingset = new TrainingSet();
        trainingset.name = marketName;
        trainingset.goal_answers = marketGoalAnswers;
        trainingset.question = "Which category would you give to the transaction description <pre>{{ trainingsample }}</pre>?";

        List<se.tink.backend.core.Category> allCategories = categoryRepository.findAll("en_US");

        Map<String, String> displayNameByCode = allCategories.stream()
                .filter(c -> c.getDisplayName() != null)
                .collect(Collectors
                        .toMap(se.tink.backend.core.Category::getCode, se.tink.backend.core.Category::getDisplayName));

        log.info("Parent entries:");
        displayNameByCode.entrySet().forEach(e -> log.info(String.format(" * %s => %s", e.getKey(), e.getValue())));

        CreateTrainingSetRequest request = new CreateTrainingSetRequest();
        request.trainingset = trainingset;
        request.categories = allCategories.stream()
                .filter(c -> c.getDisplayName() != null)
                .filter(c -> c.getCode().startsWith(categoryCodePrefix))
                .filter(c -> c.getCode().contains(".")) // HACK: Excludes all parent categories.
                .map(c -> {
                    Category category = new Category();
                    boolean isOther = c.getCode().endsWith(".other");
                    category.description = isOther ? "Other" : c.getDisplayName();
                    if (isOther) {
                        // Since no other category for this display_group have a sort key, this will end up sorted last.
                        category.sort_key = "Other";
                    }
                    category.id = c.getCode();

                    // HACK: There's probably a smarter way to extract the parent category than this...
                    String parentCode = PERIOD_SPLITTER.split(c.getCode()).iterator().next();
                    log.debug(String.format("Trying to find parent code '%s'.", parentCode));
                    category.display_group = Preconditions.checkNotNull(
                            displayNameByCode.get(parentCode));

                    return category;
                }).collect(Collectors.toList());

        Category dontKnowCategory = new Category();
        dontKnowCategory.id = "misc.uncategorized";
        dontKnowCategory.description = "Don't know";
        request.categories.add(dontKnowCategory);

        CreateTrainingSetResponse response = categorService.create("Bearer " + apiToken, request);
        log.info("TrainingSet created. Identifier: " + response.key);
    }

    private String getRequiredSystemProperty(String property) {
        return Preconditions
                .checkNotNull(System.getProperty(property), String.format("'%s' must be specified", property));
    }

    private static class TrainingSet {
        public String name;
        public String question;
        public int goal_answers;
    }

    private static class Category {
        public String id;
        public String description;
        public String display_group;
        public String sort_key;
    }

    private static class CreateTrainingSetRequest {
        public TrainingSet trainingset;
        public List<Category> categories;
    }

    private static class CreateTrainingSetResponse {
        public String key;
    }

    @Path("/api/training-sets")
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @Produces({
            MediaType.APPLICATION_JSON
    })
    private interface TrainingSetService {
        @POST
        @Path("")
        @Consumes({
                MediaType.APPLICATION_JSON
        })
        @Produces({
                MediaType.APPLICATION_JSON
        })
        CreateTrainingSetResponse create(
                @HeaderParam("Authorization") String authorizationHeader,
                CreateTrainingSetRequest request
        );
    }

}
