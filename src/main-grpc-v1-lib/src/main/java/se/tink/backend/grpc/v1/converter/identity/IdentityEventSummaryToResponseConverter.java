package se.tink.backend.grpc.v1.converter.identity;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.IdentityAnswerKey;
import se.tink.grpc.v1.models.IdentityDocumentedAnswer;
import se.tink.grpc.v1.models.IdentityEventSummary;

public class IdentityEventSummaryToResponseConverter implements Converter<se.tink.libraries.identity.model.IdentityEventSummary, IdentityEventSummary> {

    @Override
    public IdentityEventSummary convertFrom(se.tink.libraries.identity.model.IdentityEventSummary input) {
        IdentityEventSummary.Builder builder = IdentityEventSummary.newBuilder();
        builder.setId(input.getId());
        builder.setDescription(input.getDescription());
        builder.setDate(ProtobufModelUtils.toProtobufTimestamp(input.getDate()));
        builder.setSeen(input.isSeen());
        ConverterUtils.setIfPresent(input::getAnswer, builder::setAnswer, a -> IdentityDocumentedAnswer.newBuilder()
                .setAnswer(EnumMappers.IDENTITY_ANSWER_KEY_TO_GRPC_ANSWER_KEY
                        .getOrDefault(a, IdentityAnswerKey.IDENTITY_ANSWER_KEY_UNKNOWN)).build());

        return builder.build();
    }

}
