package se.tink.backend.insights.identity;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.fraud.IdentityEventUtils;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.identity.model.IdentityEvent;

public class IdentityQueryServiceImpl implements IdentityQueryService {
    private final FraudDetailsContentType fraudDetailsContentAddressType = FraudDetailsContentType.ADDRESS;
    private FraudDetailsRepository fraudDetailsRepository;
    private CurrenciesByCodeProvider currenciesByCodeProvider;

    @Inject
    public IdentityQueryServiceImpl(FraudDetailsRepository fraudDetailsRepository,
            CurrenciesByCodeProvider currenciesByCodeProvider) {
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.currenciesByCodeProvider = currenciesByCodeProvider;
    }

    public List<IdentityEvent> getFraudIdentityEvents(UserId userId, String localeStr, String currencyCode) {
        Currency currency = currenciesByCodeProvider.get().get(currencyCode);
        Locale locale = I18NUtils.getLocale(localeStr);
        return getFraudIdentityEvents(userId, locale, currency);
    }

    private List<IdentityEvent> getFraudIdentityEvents(UserId userId, Locale locale, Currency currency) {
        List<FraudDetails> allFraudDetails = fraudDetailsRepository.findAllByUserId(userId.value());

        return allFraudDetails.stream()
                .filter(fd -> !Objects.equal(fd.getStatus(), FraudStatus.OK))
                .filter(fd -> !Objects.equal(fd.getAnswers(), FraudStatus.EMPTY))
                .map(fd -> IdentityEventUtils.enrichFraudDetails(fd, locale, currency))
                .map(fd -> IdentityEventUtils.mapIdentityEvent(locale.toString(), fd))
                .collect(Collectors.toList());
    }

    public Optional<FraudDetails> getFraudAddressDetails(UserId userId) {
        return fraudDetailsRepository
                .findAllByUserIdAndType(userId.value(), fraudDetailsContentAddressType)
                .stream().findFirst();

    }
}
