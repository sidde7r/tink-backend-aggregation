package se.tink.backend.grpc.v1.converter.consent;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.Link;
import se.tink.backend.consent.core.Message;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ConsentMessage;
import se.tink.grpc.v1.models.ConsentMessageLink;

public class GrpcConsentConverter {

    public static List<se.tink.grpc.v1.models.Consent> convert(List<Consent> input) {
        return input.stream().map(GrpcConsentConverter::convert).collect(Collectors.toList());
    }

    public static se.tink.grpc.v1.models.Consent convert(Consent input) {
        se.tink.grpc.v1.models.Consent.Builder builder = se.tink.grpc.v1.models.Consent.newBuilder();

        ConverterUtils.setIfPresent(input::getBody, builder::setBody);
        ConverterUtils.setIfPresent(input::getChecksum, builder::setChecksum);
        ConverterUtils.setIfPresent(input::getVersion, builder::setVersion);
        ConverterUtils.setIfPresent(input::getKey, builder::setKey);
        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        ConverterUtils.setIfPresent(input::getAttachments, builder::putAllAttachments);
        ConverterUtils.setIfPresent(input::getMessages, builder::addAllMessages, GrpcConsentConverter::convertMessages);

        return builder.build();
    }

    private static List<ConsentMessage> convertMessages(List<Message> messages) {
        return messages.stream().map(GrpcConsentConverter::convert).collect(Collectors.toList());
    }

    private static ConsentMessage convert(Message message) {
        ConsentMessage.Builder builder = ConsentMessage.newBuilder();
        ConverterUtils.setIfPresent(message::getMessage, builder::setMessage);
        ConverterUtils.setIfPresent(message::getLinks, builder::addAllLinks, GrpcConsentConverter::convertLinks);

        return builder.build();
    }

    private static List<ConsentMessageLink> convertLinks(List<Link> links) {
        return links.stream().map(GrpcConsentConverter::convert).collect(Collectors.toList());
    }

    private static ConsentMessageLink convert(Link link) {
        return ConsentMessageLink.newBuilder()
                .setTo(link.getDestination())
                .setStart(link.getStart())
                .setEnd(link.getEnd())
                .build();
    }
}
