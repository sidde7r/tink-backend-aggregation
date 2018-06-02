package se.tink.backend.common.application.field;

import se.tink.libraries.i18n.LocalizableKey;

public class LocalizableInfoSection extends InfoSection<LocalizableKey, LocalizableKey> {
    public LocalizableInfoSection(LocalizableKey title, LocalizableKey body) {
        super(title, body);
    }
}
