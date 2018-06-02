package se.tink.backend.categorization;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Test;
import se.tink.backend.core.CategorizationCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.core.CategorizationCommand.GENERAL_EXPENSES;
import static se.tink.backend.core.CategorizationCommand.USER_LEARNING;

public class ProbabilityCategorizerTest {

    @Test
    public void checkMapCategoryCodesToCategorizationCommands() {
        Map<CategorizationCommand, CategorizationVector> initMap = Maps.newHashMap();
        initMap.put(USER_LEARNING, new CategorizationVector(1, createDistributionMap("food", "home")));
        initMap.put(GENERAL_EXPENSES, new CategorizationVector(1, createDistributionMap("home", "communication")));

        Multimap<String, CategorizationCommand> categoryCodesToCategorizationCommands = ProbabilityCategorizer
                .categoryCodesToCategorisationCommands(initMap);

        Multimap<String, CategorizationCommand> expectedMap = ArrayListMultimap.create();
        expectedMap.put("food", USER_LEARNING);
        expectedMap.put("communication", GENERAL_EXPENSES);
        expectedMap.putAll("home", Arrays.asList(USER_LEARNING, GENERAL_EXPENSES));

        assertThat(categoryCodesToCategorizationCommands.keySet().size()).isEqualTo(3);
        assertThat(categoryCodesToCategorizationCommands.get("food")).containsOnly(USER_LEARNING);
        assertThat(categoryCodesToCategorizationCommands.get("communication")).containsOnly(GENERAL_EXPENSES);
        assertThat(categoryCodesToCategorizationCommands.get("home")).containsOnly(USER_LEARNING, GENERAL_EXPENSES);
    }

    private Map<String, Double> createDistributionMap(String... keys) {
        return Maps.toMap(Arrays.asList(keys), new Function<String, Double>() {
            @Nullable
            @Override
            public Double apply(String s) {
                return 1.;
            }
        });
    }

    @Test
    public void checkCategorisationCommandByCategoryCode() {
        Multimap<String, CategorizationCommand> codeToCategorizationCommandMap = ArrayListMultimap.create();
        codeToCategorizationCommandMap.put("food", GENERAL_EXPENSES);
        codeToCategorizationCommandMap.put("communication", USER_LEARNING);
        codeToCategorizationCommandMap.put("home", GENERAL_EXPENSES);

        Collection<CategorizationCommand> categorizationCommands = ProbabilityCategorizer
                .getCategorisationCommandsByCategoryCodes(codeToCategorizationCommandMap,
                        Arrays.asList("food", "home"));

        assertThat(categorizationCommands).containsOnly(GENERAL_EXPENSES);
    }
}
