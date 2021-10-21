package se.tink.backend.aggregation.agents.exceptions.connectivity;

import se.tink.libraries.i18n.LocalizableKey;

interface ConnectivityErrorDefaultMessageMapper<T extends Enum<T>> {

    LocalizableKey map(T t);
}
