package se.tink.backend.aggregation.agents.general;

import static com.google.common.base.Objects.equal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class GeneralUtils {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    private GeneralUtils() {
        throw new AssertionError();
    }

    @SafeVarargs
    public static List<GeneralAccountEntity> concat(
            List<? extends GeneralAccountEntity>... accounts) {
        return Stream.of(accounts).flatMap(List::stream).distinct().collect(Collectors.toList());
    }

    public static <T extends GeneralAccountEntity> Optional<T> find(
            final AccountIdentifier identifier, List<T> entities) {
        return entities.stream()
                .filter(
                        gae ->
                                equal(
                                        gae.generalGetAccountIdentifier()
                                                .getIdentifier(DEFAULT_FORMATTER),
                                        identifier.getIdentifier(DEFAULT_FORMATTER)))
                .findFirst();
    }

    public static <T extends GeneralAccountEntity> boolean isAccountExisting(
            AccountIdentifier identifier, List<T> entities) {
        return find(identifier, entities).isPresent();
    }
}
