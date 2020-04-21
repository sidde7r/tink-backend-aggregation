package se.tink.backend.aggregation.agents.framework.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.comparor.Comparor;
import se.tink.backend.aggregation.comparor.DifferenceEntity;
import se.tink.backend.aggregation.comparor.EmptyDifferenceEntity;
import se.tink.backend.aggregation.comparor.ListDifferenceEntity;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class AgentContractEntitiesAsserts {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(AgentContractEntitiesAsserts.class);

    public static <A, B> boolean areListsMatchingVerbose(List<A> expected, List<B> given) {
        Comparor comparor = new Comparor(new ContractEntityDifferenceCounter());

        DifferenceEntity difference = comparor.areListsMatching(expected, given);
        if (difference instanceof EmptyDifferenceEntity) {
            log.info("Lists are matching");
            return true;
        } else if (difference instanceof ListDifferenceEntity) {
            log.error("Size of the lists does not match!");
            return false;
        } else if (difference instanceof MapDifferenceEntity) {
            MapDifferenceEntity diff = (MapDifferenceEntity) difference;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    "The following object in expected list could not be matched with anything in the given list\n");
            stringBuilder.append("Expected Object\n");
            stringBuilder.append(diff.getSerializedExpectedMap() + "\n");
            stringBuilder.append("The closest given object is the following\n");
            stringBuilder.append(diff.getSerializedGivenMap() + "\n");
            stringBuilder.append("The differences are the following:\n");
            if (diff.getEntriesOnlyOnExpected().size() > 0) {
                stringBuilder.append("The following keys only appear in expected object\n");
                diff.getEntriesOnlyOnExpected()
                        .keySet()
                        .forEach(key -> stringBuilder.append(key + "\n"));
            }
            if (diff.getDifferenceInCommonKeys().size() > 0) {
                stringBuilder.append(
                        "For the following keys the expected and given objects have different values\n");
                diff.getDifferenceInCommonKeys()
                        .keySet()
                        .forEach(key -> stringBuilder.append(key + "\n"));
            }
            log.error(stringBuilder.toString());
            return false;
        } else {
            throw new IllegalStateException("Difference type could not be handled");
        }
    }
}
