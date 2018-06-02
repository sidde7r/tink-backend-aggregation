package se.tink.backend.aggregation.agents.utils.typeguesser;

import com.google.common.collect.ImmutableList;

class NorwegianTypeGuesser extends TypeGuesserBase {

    NorwegianTypeGuesser() {
        super(ImmutableList.of("bsu", "spar"), ImmutableList.of());
    }
}
