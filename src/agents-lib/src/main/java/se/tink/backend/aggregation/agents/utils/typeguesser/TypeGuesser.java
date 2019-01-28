package se.tink.backend.aggregation.agents.utils.typeguesser;

import se.tink.backend.agents.rpc.AccountTypes;

/**
 * Language based type guesser.
 * Any guessing is implemented in the language specific guessers.
 */
public enum TypeGuesser {
    SWEDISH(new SwedishTypeGuesser()),
    DANISH(new DanishTypeGuesser()),
    NORWEGIAN(new NorwegianTypeGuesser());

    private TypeGuesser(TypeGuesserBase typeGuesser) {
        this.typeGuesser = typeGuesser;
    }

    private final TypeGuesserBase typeGuesser;

    public AccountTypes guessAccountType(String accountName) {
        return typeGuesser.guessAccountType(accountName);
    }
}
