package se.tink.backend.grpc.v1.converter.identity;

import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.transaction.CoreTransactionToGrpcTransactionConverter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.IdentityAnswerKey;
import se.tink.grpc.v1.models.IdentityDocumentedAnswer;
import se.tink.grpc.v1.models.IdentityEvent;
import se.tink.grpc.v1.models.IdentityEventAnswer;
import se.tink.grpc.v1.models.IdentityEventDocumentation;

public class IdentityEventConverter implements Converter<se.tink.libraries.identity.model.IdentityEvent, IdentityEvent> {
    private final CoreTransactionToGrpcTransactionConverter transactionConverter;

    public IdentityEventConverter(String currencyCode, Map<String, String> categoryCodeById) {
        transactionConverter = new CoreTransactionToGrpcTransactionConverter(currencyCode, categoryCodeById);
    }

    @Override
    public IdentityEvent convertFrom(se.tink.libraries.identity.model.IdentityEvent input) {
        IdentityEvent.Builder builder = IdentityEvent.newBuilder();
        builder.setId(input.getId());
        builder.setDate(ProtobufModelUtils.toProtobufTimestamp(input.getDate()));
        builder.setDescription(input.getDescription());
        builder.setSeen(input.isSeen());
        builder.setQuestion(input.getQuestion());
        builder.addAllPotentialAnswers(input.getAnswers().stream()
                .map(a -> IdentityEventAnswer.newBuilder().setLabel(a.getText())
                        .setKey(EnumMappers.IDENTITY_ANSWER_KEY_TO_GRPC_ANSWER_KEY.get(a.getKey())).build())
                .collect(Collectors.toList()));

        ConverterUtils.setIfPresent(input::getAnswer, builder::setAnswer, a -> IdentityDocumentedAnswer.newBuilder()
                .setAnswer(EnumMappers.IDENTITY_ANSWER_KEY_TO_GRPC_ANSWER_KEY
                        .getOrDefault(a, IdentityAnswerKey.IDENTITY_ANSWER_KEY_UNKNOWN)).build());

        ConverterUtils.setIfPresent(input::getDocumentation, builder::setDocumentation,
                d -> IdentityEventDocumentation.newBuilder()
                        .setSource(d.getSourceTitle() + " " + d.getSourceText())
                        .setInfoTitle(d.getInfoTitle())
                        .setInfoBody(d.getInfoText())
                        .setHelpTitle(d.getHelpTitle())
                        .setHelpBody(d.getHelpText())
                        .build());

        builder.addAllTransactions(transactionConverter.convertFrom(input.getTransactions()));

        return builder.build();
    }
}
