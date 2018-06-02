package se.tink.backend.grpc.v1.converter.insights;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.insights.http.dto.InsightDTO;
import se.tink.grpc.v1.models.Insight;

public class InsightGrpcConverter implements Converter<InsightDTO, Insight>{

    @Override
    public Insight convertFrom(InsightDTO input) {
        // TODO: write converter + button converter
        return null;
    }
}
