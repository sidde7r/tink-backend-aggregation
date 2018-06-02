package se.tink.backend.common.dao;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import se.tink.backend.common.repository.cassandra.ProductFilterRepository;
import se.tink.backend.common.repository.cassandra.ProductInstanceRepository;
import se.tink.backend.common.repository.cassandra.ProductTemplateRepository;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductFilter;
import se.tink.backend.core.product.ProductFilterStatus;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductTemplateStatus;
import se.tink.backend.core.product.ProductType;

public class ProductDAO {

    public static final Predicate<ProductFilter> FILTER_IS_ENABLED = filter -> Objects
            .equal(ProductFilterStatus.ENABLED, filter.getStatus());

    public static final Predicate<ProductTemplate> TEMPLATE_IS_ENABLED = template -> Objects
            .equal(ProductTemplateStatus.ENABLED, template.getStatus());
    
    private final ProductFilterRepository productFilterRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final ProductTemplateRepository productTemplateRepository;

    @Inject
    public ProductDAO(ProductFilterRepository productFilterRepository,
            ProductInstanceRepository productInstanceRepository, ProductTemplateRepository productTemplateRepository) {
        this.productFilterRepository = productFilterRepository;
        this.productInstanceRepository = productInstanceRepository;
        this.productTemplateRepository = productTemplateRepository;
    }
    
    /*
     * Disables an instance by setting the validity end date to now.
     */
    public void disableProductInstance(UUID userId, UUID productInstanceId) {
        ProductInstance instance = productInstanceRepository.findByUserIdAndId(userId, productInstanceId);
        if (instance != null) {
            instance.setValidTo(new Date());
            save(instance);
        }
    }
    
    public void disableProductInstancesOfType(UUID userId, ProductType type) {
        for (ProductArticle article : findAllActiveArticlesByUserIdAndType(userId, type)) {
            disableProductInstance(userId, article.getInstanceId());
        }
    }
    
    public List<ProductFilter> findAllEnabledFilters() {
        return Lists.newArrayList(FluentIterable.from(productFilterRepository.findAll()).filter(FILTER_IS_ENABLED));
    }
    
    public List<ProductTemplate> findAllEnabledTemplates() {
        return Lists.newArrayList(FluentIterable.from(productTemplateRepository.findAll()).filter(TEMPLATE_IS_ENABLED));
    }
    
    public List<ProductArticle> findAllArticlesByUserId(UUID userId) {
        List<ProductArticle> articles = Lists.newArrayList();
        
        List<ProductInstance> instances = productInstanceRepository.findAllByUserId(userId);
        
        if (instances != null) {
            for (ProductInstance instance : instances) {
                ProductTemplate template = productTemplateRepository.findById(instance.getTemplateId());
                if (template != null) {
                    articles.add(new ProductArticle(template, instance));
                }
            }
        }
        
        return articles;
    }
    
    public List<ProductArticle> findAllActiveArticlesByUserId(UUID userId) {
        List<ProductArticle> articles = Lists.newArrayList();
        
        final Date now = new Date();
        
        for (ProductArticle article : findAllArticlesByUserId(userId)) {
            if (!Objects.equal(ProductTemplateStatus.ENABLED, article.getStatus())) {
                continue;
            }
            
            if (article.getValidFrom() != null && now.before(article.getValidFrom())) {
                continue;
            }
            
            if (article.getValidTo() != null && now.after(article.getValidTo())) {
                continue;
            }
            
            articles.add(article);
        }
        
        return articles;
    }
    
    public List<ProductArticle> findAllActiveArticlesByUserIdAndType(UUID userId, ProductType type) {
        List<ProductArticle> articles = Lists.newArrayList();
        
        for (ProductArticle article : findAllActiveArticlesByUserId(userId)) {
            if (Objects.equal(type, article.getType())) {
                articles.add(article);                
            }
        }
        
        return articles;
    }
    
    public ProductFilter findFilterByTemplateIdAndId(UUID templateId, UUID filterId) {
        return productFilterRepository.findByTemplateIdAndId(templateId, filterId);
    }

    public ProductInstance findInstanceByUserIdAndId(UUID userId, UUID productInstanceId) {
        return productInstanceRepository.findByUserIdAndId(userId, productInstanceId);
    }
    
    public ProductArticle findArticleByUserIdAndId(UUID userId, UUID productInstanceId) {
        
        ProductInstance instance = productInstanceRepository.findByUserIdAndId(userId, productInstanceId);
        
        if (instance == null) {
            return null;
        }
        
        ProductTemplate template = productTemplateRepository.findById(instance.getTemplateId());
        
        if (template == null) {
            return null;
        }
        
        return new ProductArticle(template, instance);
    }
    
    public ProductFilter save(ProductFilter filter) {
        return productFilterRepository.save(filter);
    }
    
    public ProductTemplate save(ProductTemplate template) {
        return productTemplateRepository.save(template);
    }
    
    public ProductInstance save(ProductInstance instance) {
        return productInstanceRepository.save(instance);
    }

    public void updateValidTo(ProductArticle productArticle, Date date) {
        ProductInstance productInstance = productInstanceRepository.findByUserIdAndId(
                productArticle.getUserId(),
                productArticle.getInstanceId());

        Preconditions.checkNotNull(productInstance);

        productInstance.setValidTo(date);

        save(productInstance);
    }
}
