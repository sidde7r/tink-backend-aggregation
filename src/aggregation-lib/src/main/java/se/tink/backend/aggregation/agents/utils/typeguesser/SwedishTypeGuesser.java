package se.tink.backend.aggregation.agents.utils.typeguesser;

import com.google.common.collect.ImmutableList;

class SwedishTypeGuesser extends TypeGuesserBase {

    SwedishTypeGuesser() {
        super(ImmutableList.of("spar"), ImmutableList.of("kapitalko", "isk "));
    }
}
