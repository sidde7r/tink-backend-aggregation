package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Lists;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.FraudDetailsContent;

public class FraudDataTransformContentToFraudDetailsProcessor extends FraudDataProcessor {

    private final FraudDetailsContentRepository detailsContentRepository;
    private final CacheClient cacheClient;

    public FraudDataTransformContentToFraudDetailsProcessor(ServiceContext context) {
        detailsContentRepository = context.getRepository(FraudDetailsContentRepository.class);
        cacheClient = context.getCacheClient();
    }

    @Override
    public void process(FraudDataProcessorContext context) {
        Iterable<FraudDetailsContent> detailsContents = detailsContentRepository.findByUserId(context.getUser().getId(), cacheClient);
        context.addFraudDetailsContent(Lists.newArrayList(detailsContents));
    }
}
