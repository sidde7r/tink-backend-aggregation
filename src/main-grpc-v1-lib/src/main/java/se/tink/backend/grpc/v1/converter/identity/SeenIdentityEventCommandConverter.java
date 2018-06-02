package se.tink.backend.grpc.v1.converter.identity;

import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.libraries.identity.commands.SeenIdentityEventCommand;
import se.tink.grpc.v1.rpc.SeenIdentityEventRequest;

public class SeenIdentityEventCommandConverter implements Converter<SeenIdentityEventRequest, SeenIdentityEventCommand> {

    private User user;
    private CurrenciesByCodeProvider currenciesByCodeProvider;

    public SeenIdentityEventCommandConverter(User user,
            CurrenciesByCodeProvider currenciesByCodeProvider) {
        this.user = user;
        this.currenciesByCodeProvider = currenciesByCodeProvider;
    }

    @Override
    public SeenIdentityEventCommand convertFrom(SeenIdentityEventRequest input) {
        return new SeenIdentityEventCommand(user.getId(), input.getIdentityEventIdsList(),
                I18NUtils.getLocale(user.getProfile().getLocale()),
                currenciesByCodeProvider.get().get(user.getProfile().getCurrency()));
    }
}
