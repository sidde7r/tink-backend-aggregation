package se.tink.backend.grpc.v1.converter.identity;

import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.libraries.identity.commands.AnswerIdentityEventCommand;
import se.tink.grpc.v1.rpc.AnswerIdentityEventRequest;

public class AnswerIdentityEventCommandConverter
        implements Converter<AnswerIdentityEventRequest, AnswerIdentityEventCommand> {

    private User user;
    private CurrenciesByCodeProvider currenciesByCodeProvider;


    public AnswerIdentityEventCommandConverter(User user,
            CurrenciesByCodeProvider currenciesByCodeProvider) {
        this.user = user;
        this.currenciesByCodeProvider = currenciesByCodeProvider;
    }

    @Override
    public AnswerIdentityEventCommand convertFrom(AnswerIdentityEventRequest input) {
        return new AnswerIdentityEventCommand(input.getIdentityEventId(), user.getId(),
                EnumMappers.IDENTITY_ANSWER_KEY_TO_GRPC_ANSWER_KEY.inverse().get(input.getAnswer().getKey()),
                I18NUtils.getLocale(user.getProfile().getLocale()),
                currenciesByCodeProvider.get().get(user.getProfile().getCurrency()));
    }
}
