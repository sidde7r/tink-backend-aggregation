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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.account.identifiers.*;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;

public abstract class AccountIdentifier {
    private static final String NAME_PARAMETER = "name";
    private static final Logger log = LoggerFactory.getLogger(AccountIdentifier.class);

    private String name;

    public enum Type {
        BE("be"),
        DE("de"),
        DK("dk"),
        SE("se"),
        SE_SHB_INTERNAL("se-internal"),
        SE_NDA_SSN("se-nda-ssn"),
        FI("fi"),
        NO("no"),
        IBAN("iban"),
        TINK("tink"),
        SE_BG("se-bg"),
        SE_PG("se-pg"),
        SEPA_EUR("sepa-eur"),
        SORT_CODE("sort-code"),
        PAYMENT_CARD_NUMBER("payment-card-number"),
        PAYM_PHONE_NUMBER("paym-phone-number"),
        PT_BPI("pt-bpi"),
        BBAN("bban"),
        COUNTRY_SPECIFIC("country_specific"),
        OTHER("other");

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

        /** String construction to be used by Jersey deserialization */
        public static Type fromString(String s) {
            return fromScheme(s);
        }
    }

    /**
     * The identifier should be the identifier without the type. To get the serialized
     * AccountIdentifier (e.g. se://1242149719742) use AccountIdentifier#toUriAsString.
     *
     * <p>The following should be true: new XIdentifier(xIdentifier.getIdentifier()).getIdentifier()
     * == xIdentifier.getIdentifier() where XIdentifier extends AccountIdentifier and xIdentifier is
     * an XIdentifier.
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

    public boolean isGiroIdentifier() {

        return false;
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
        if (!(obj instanceof AccountIdentifier)) {
            return false;
        }

        AccountIdentifier id = (AccountIdentifier) obj;

        if (getType() != id.getType()) {
            return false;
        }

        return Objects.equal(getIdentifier(), id.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getIdentifier());
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

        return params.stream()
                .filter(AccountIdentifierPredicate.NAMEVALUEPAIR_NAME_PARAMETER::apply)
                .findFirst()
                .map(NameValuePair::getValue);
    }

    public static AccountIdentifier create(Type type, String id) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case BE:
                return new BelgianIdentifier(id);
            case DK:
                return new DanishIdentifier(id);
            case SE:
                return new SwedishIdentifier(id);
            case SE_SHB_INTERNAL:
                return new SwedishSHBInternalIdentifier(id);
            case SE_NDA_SSN:
                return new NDAPersonalNumberIdentifier(id);
            case FI:
                return new FinnishIdentifier(id);
            case NO:
                return new NorwegianIdentifier(id);
            case TINK:
                return new TinkIdentifier(id);
            case IBAN:
                return new IbanIdentifier(id);
            case SE_BG:
            case COUNTRY_SPECIFIC:
                return new BankGiroIdentifier(id);
            case SE_PG:
                return new PlusGiroIdentifier(id);
            case SEPA_EUR:
                return new SepaEurIdentifier(id);
            case SORT_CODE:
                return new SortCodeIdentifier(id);
            case PAYM_PHONE_NUMBER:
                return new PaymPhoneNumberIdentifier(id);
            case PAYMENT_CARD_NUMBER:
                return new PaymentCardNumberIdentifier(id);
            case BBAN:
                return new BbanIdentifier(id);
            case PT_BPI:
                return new PortugalBancoBpiIdentifier(id);
            case DE:
                return new GermanIdentifier(id);
            case OTHER:
                return new OtherIdentifier(id);
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
