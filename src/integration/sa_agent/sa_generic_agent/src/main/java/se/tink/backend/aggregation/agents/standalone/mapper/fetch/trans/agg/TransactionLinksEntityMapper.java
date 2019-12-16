package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg;

import org.apache.commons.lang3.StringUtils;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class TransactionLinksEntityMapper
        implements Mapper<String, se.tink.sa.services.fetch.trans.TransactionLinksEntity> {
    @Override
    public String map(
            se.tink.sa.services.fetch.trans.TransactionLinksEntity source,
            MappingContext mappingContext) {
        if (StringUtils.isBlank(source.getNext())) {
            return null;
        }
        return source.getNext();
    }
}
