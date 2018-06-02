package se.tink.backend.grpc.v1.converter.identity;

import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.libraries.identity.commands.GetIdentityEventSummaryListCommand;

public class GetIdentityEventSummaryCommandConverter implements Converter<User, GetIdentityEventSummaryListCommand> {

    private CurrenciesByCodeProvider currenciesByCodeProvider;


    public GetIdentityEventSummaryCommandConverter(CurrenciesByCodeProvider currenciesByCodeProvider) {
        this.currenciesByCodeProvider = currenciesByCodeProvider;
    }

    @Override
    public GetIdentityEventSummaryListCommand convertFrom(User input) {
        return new GetIdentityEventSummaryListCommand(
                input.getId(),
                I18NUtils.getLocale(input.getProfile().getLocale()),
                currenciesByCodeProvider.get().get(input.getProfile().getCurrency())
        );
    }
}
