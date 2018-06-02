package se.tink.backend.consent.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.xerial.snappy.Snappy;
import se.tink.backend.consent.cache.ConsentCache;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.Message;
import se.tink.backend.consent.core.User;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.core.cassandra.CassandraUserConsent;
import se.tink.backend.consent.core.exceptions.ConsentNotFoundException;
import se.tink.backend.consent.core.exceptions.InvalidChecksumException;
import se.tink.backend.consent.core.exceptions.UserConsentNotFoundException;
import se.tink.backend.consent.repository.cassandra.UserConsentRepository;
import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.backend.consent.utils.ConsentFilters;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static se.tink.libraries.uuid.UUIDUtils.fromTinkUUID;

public class ConsentDAO {
    private final UserConsentRepository userConsentRepository;
    private final ConsentCache consentCache;

    private static final ModelMapper mapper = new ModelMapper();

    static {
        Converter<UUID, String> uuidToString = ctx -> UUIDUtils.toTinkUUID(ctx.getSource());

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.createTypeMap(CassandraConsent.class, Consent.class);
        mapper.createTypeMap(CassandraUserConsent.class, UserConsent.class)
                .addMappings(m -> m.using(uuidToString).map(CassandraUserConsent::getId, UserConsent::setId));
        mapper.createTypeMap(String.class, Message.class)
                .setConverter(ctx -> SerializationUtils.deserializeFromString(ctx.getSource(), Message.class));
    }

    @Inject
    public ConsentDAO(ConsentCache consentCache, UserConsentRepository userConsentRepository) {
        this.userConsentRepository = userConsentRepository;
        this.consentCache = consentCache;
    }

    public UserConsent findByUserIdAndId(String userId, String id) throws UserConsentNotFoundException {
        CassandraUserConsent consent = userConsentRepository.findByUserIdAndId(fromTinkUUID(userId), fromTinkUUID(id));

        if (consent == null) {
            throw new UserConsentNotFoundException(userId, id);
        }

        return mapper.map(consent, UserConsent.class);
    }

    public List<UserConsent> findLatestByUserId(String userId) {
        List<CassandraUserConsent> all = userConsentRepository.findAllByUserId(fromTinkUUID(userId));

        List<CassandraUserConsent> filtered = ConsentFilters.filterLatestUserConsents(all);

        return filtered.stream().map(c -> mapper.map(c, UserConsent.class)).collect(Collectors.toList());
    }

    public List<Consent> findAllAvailableByUserIdAndLocale(String userId, String locale) {
        ImmutableList<CassandraConsent> all = consentCache.getLatestByLocale(locale);

        List<UserConsent> userConsents = findLatestByUserId(userId);

        return all.stream()
                .filter(cassandraConsent -> userConsents.stream().noneMatch(x -> x.isCompatibleWith(cassandraConsent)))
                .map(result -> mapper.map(result, Consent.class))
                .collect(Collectors.toList());
    }

    public UserConsent saveUserConsent(User user, ConsentRequest request, Signature signature)
            throws ConsentNotFoundException, InvalidChecksumException, SignatureException, IOException {

        Optional<CassandraConsent> consent = consentCache.get(request.getKey(), request.getVersion(), user.getLocale());

        if (!consent.isPresent()) {
            throw new ConsentNotFoundException(request.getKey(), request.getVersion(), user.getLocale());
        }

        if (!Objects.equals(consent.get().getChecksum(), request.getChecksum())) {
            throw new InvalidChecksumException(consent.get().getChecksum(), request.getChecksum());
        }

        String payload = SerializationUtils.serializeToString(consent.get());

        CassandraUserConsent cassandraUserConsent = CassandraUserConsent.builder()
                .withConsent(consent.get())
                .withAction(request.getAction())
                .withUserId(UUIDUtils.fromTinkUUID(user.getId()))
                .withUsername(user.getUsername())
                .withSignature(signature)
                .withPayload(Snappy.compress(payload))
                .build();

        return mapper.map(userConsentRepository.save(cassandraUserConsent), UserConsent.class);
    }

    public Consent getLatestByKeyAndLocale(String locale, String key) throws ConsentNotFoundException {
        Optional<CassandraConsent> latest = consentCache.getLatestByKeyAndLocale(locale, key);

        if (latest.isPresent()) {
            return mapper.map(latest.get(), Consent.class);
        }

        throw new ConsentNotFoundException(key, null, locale);
    }

    public static ModelMapper getModelMapper() {
        return mapper;
    }
}
