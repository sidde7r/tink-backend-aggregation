package se.tink.libraries.account;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.BelgianIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.log.LogUtils;

public abstract class AccountIdentifier {
    private static final String NAME_PARAMETER = "name";
    private static final LogUtils log = new LogUtils(AccountIdentifier.class);

    private String name;

    public enum Type {
        BE("be"),
        SE("se"),
        SE_SHB_INTERNAL("se-internal"),
        FI("fi"),
        IBAN("iban"),
        TINK("tink"),
        SE_BG("se-bg"),
        SE_PG("se-pg"),
        SEPA_EUR("sepa-eur"),
        SORT_CODE("sort-code");

        private String scheme;
        Type(String scheme) {
            this.scheme = scheme;
        }
        @Override
        public String toString() {
            return scheme;
        }

        public static Type fromScheme(String scheme) {
            if (scheme != null) {
                for (Type type : Type.values()) {
                    if (scheme.equalsIgnoreCase(type.scheme)) {
                        return type;
                    }
                }
            }
            return null;
        }

        /**
         * String construction to be used by Jersey deserialization
         */
        public static Type fromString(String s) {
            return fromScheme(s);
        }
    }

    /**
     * The identifier should be the identifier without the type. To get the serialized AccountIdentifier (e.g.
     * se://1242149719742) use AccountIdentifier#toUriAsString.
     * <p/>
     * The following should be true: new XIdentifier(xIdentifier.getIdentifier()).getIdentifier() ==
     * xIdentifier.getIdentifier() where XIdentifier extends AccountIdentifier and xIdentifier is an XIdentifier.
     *
     * @return an identifier
     */
    public abstract String getIdentifier();

    public String getIdentifier(AccountIdentifierFormatter formatter) {
        return formatter.apply(this);
    }

    public abstract boolean isValid();
    public abstract Type getType();

    public <T extends AccountIdentifier> T to(Class<T> cls) {
        return cls.cast(this);
    }

    public boolean is(Type type) {
        return getType() == type;
    }

    public URI toURI() {
        if (!isValid()) {
            return null;
        }

        URI uriWithoutName = toURIWithoutName();

        Optional<String> name = getName();
        if (name.isPresent()) {
            try {
                return new URIBuilder(uriWithoutName)
                        .addParameter(NAME_PARAMETER, name.get())
                        .build();
            } catch (URISyntaxException e) {
                log.error("Could not create URI for AccountIdentifier", e);
            }
        }

        return uriWithoutName;
    }

    public URI toURIWithoutName() {
        if (!isValid()) {
            return null;
        }

        try {
            return new URIBuilder()
                    .setScheme(getType().toString())
                    .setHost(getIdentifier())
                    .build();
        } catch (URISyntaxException e) {
            log.error("Could not create URI for AccountIdentifier", e);
            return null;
        }
    }

    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            name = null;
        }

        this.name = name;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AccountIdentifier)) {
            return false;
        }

        AccountIdentifier id = (AccountIdentifier)obj;

        if (getType() != id.getType()) {
            return false;
        }

        return Objects.equal(getIdentifier(), id.getIdentifier());
    }

    public String toUriAsString() {
        URI uri = toURI();

        if (uri == null) {
            return MoreObjects.toStringHelper(this)
                    .add("type", getType())
                    .add("identifier", getIdentifier())
                    .toString();
        }

        return uri.toString();
    }

    @Override
    public String toString() {
        URI uri = toURI();

        if (uri == null) {
            return MoreObjects.toStringHelper(this)
                    .add("type", getType())
                    .add("identifier", maskHalfString(getIdentifier()))
                    .toString();
        }

        return uri.toString();
    }

    private String maskHalfString(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return "";
        }
        String firstHalf = s.substring(0, s.length() / 2);
        return StringUtils.rightPad(firstHalf, s.length(), '*');
    }

    public static AccountIdentifier create(URI uri) {
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }

        String identifier = uri.getHost() + uri.getPath();
        Optional<String> name = getNameFromUri(uri);

        return create(Type.fromScheme(scheme), identifier, name.orElse(null));
    }

    private static Optional<String> getNameFromUri(URI uri) {
        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");

        return params.stream().filter(AccountIdentifierPredicate.NAMEVALUEPAIR_NAME_PARAMETER::apply).findFirst()
                .map(NameValuePair::getValue);
    }

    public static AccountIdentifier create(Type type, String id) {
        if (type == null) {
            return null;
        }

        switch(type) {
        case BE:
            return new BelgianIdentifier(id);
        case SE:
            return new SwedishIdentifier(id);
        case SE_SHB_INTERNAL:
            return new SwedishSHBInternalIdentifier(id);
        case FI:
            return new FinnishIdentifier(id);
        case TINK:
            return new TinkIdentifier(id);
        case IBAN:
            return new IbanIdentifier(id);
        case SE_BG:
            return new BankGiroIdentifier(id);
        case SE_PG:
            return new PlusGiroIdentifier(id);
        case SEPA_EUR:
            return new SepaEurIdentifier(id);
        case SORT_CODE:
            return new SortCodeIdentifier(id);
        }
        return null;
    }

    public static AccountIdentifier create(Type type, String id, String name) {
        AccountIdentifier identifier = create(type, id);

        if (identifier != null) {
            identifier.setName(name);
        }

        return identifier;
    }
}
