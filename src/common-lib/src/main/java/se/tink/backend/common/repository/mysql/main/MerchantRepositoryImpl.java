package se.tink.backend.common.repository.mysql.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.common.search.strategies.CoordinateNamingStrategy;
import se.tink.backend.core.Merchant;
import se.tink.backend.utils.LogUtils;

public class MerchantRepositoryImpl implements MerchantRepositoryCustom {

    private static final LogUtils log = new LogUtils(MerchantRepositoryImpl.class);
    protected static final ObjectMapper mapper = new ObjectMapper();

    public MerchantRepositoryImpl() {
        // Strategy will rename latitude and longitude properties to work with elastic search
        mapper.setPropertyNamingStrategy(new CoordinateNamingStrategy());
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Merchant saveAndIndex(Merchant merchantSave) {
        Merchant merchant = em.merge(merchantSave);
        index(merchant);
        return merchant;
    }

    @Override
    public void removeFromIndex(String id) {
        try {

            Client searchClient = SearchProxy.getInstance().getClient();

            log.info("remove Merchant with id:" + id + " from index");

            DeleteRequestBuilder request = searchClient.prepareDelete("merchants", "merchant", id);

            DeleteResponse rsp = request.execute().actionGet();

            if (rsp.isNotFound()) {
                log.info("\tmerchant with ID=" + id + " not found.");
            }

        } catch (Exception e) {
            log.error("\tcould not remove merchant: " + id + " from index", e);
        }
    }

    public void index(Merchant m) {
        try {
            Client searchClient = SearchProxy.getInstance().getClient();

            log.info("indexMerchant");
            String content = mapper.writeValueAsString(m);

            log.info("\tindexing: " + content);

            IndexRequestBuilder request = searchClient.prepareIndex("merchants", "merchant", m.getId())
                    .setSource(content);

            request.setRefresh(true);

            request.execute().actionGet();

        } catch (Exception e) {
            log.error("\tcould not index merchant: " + m.getName(), e);
        }
    }

    @Override
    @Transactional
    public void saveAndIndex(List<Merchant> merchants) {
        for (Merchant m : merchants) {
            saveAndIndex(m);
        }
    }
}
