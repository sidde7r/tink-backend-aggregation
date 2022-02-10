package se.tink.backend.aggregation.agents.exceptions.connectivity;

import se.tink.libraries.i18n_aggregation.LocalizableKey;

interface ConnectivityErrorDefaultMessageMapper<T extends Enum<T>> {

    LocalizableKey map(T t);
}
