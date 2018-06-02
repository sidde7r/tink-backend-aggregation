package se.tink.backend.grpc.v1.converter.identity;

import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.libraries.identity.commands.GetIdentityEventCommand;
import se.tink.grpc.v1.rpc.GetIdentityEventRequest;

public class GetIdentityEventToCommandConverter implements Converter<GetIdentityEventRequest, GetIdentityEventCommand> {

    private User user;
    private CurrenciesByCodeProvider currenciesByCodeProvider;


    public GetIdentityEventToCommandConverter(User user,
            CurrenciesByCodeProvider currenciesByCodeProvider) {
        this.user = user;
        this.currenciesByCodeProvider = currenciesByCodeProvider;
    }

    @Override
    public GetIdentityEventCommand convertFrom(GetIdentityEventRequest input) {
        return new GetIdentityEventCommand(
                input.getIdentityEventId(),
                user.getId(),
                I18NUtils.getLocale(user.getProfile().getLocale()),
                currenciesByCodeProvider.get().get(user.getProfile().getCurrency())
        );
    }
}
