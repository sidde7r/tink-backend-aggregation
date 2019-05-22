package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq;

import java.util.Objects;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.entities.AliasEntity;

public class BunqPredicates {
    public static final Predicate<AliasEntity> FILTER_IBAN =
            a ->
                    a.getType() != null
                            && Objects.equals(
                                    BunqBaseConstants.PredicatesKeys.IBAN_ALIAS_TYPE,
                                    a.getType().toLowerCase());
}
