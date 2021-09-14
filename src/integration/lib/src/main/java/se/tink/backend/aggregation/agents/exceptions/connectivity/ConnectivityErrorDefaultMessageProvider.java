package se.tink.backend.aggregation.agents.exceptions.connectivity;

import se.tink.libraries.i18n.LocalizableKey;

interface ConnectivityErrorDefaultMessageProvider<T extends Enum<T>> {

    LocalizableKey provide(T t);
}
