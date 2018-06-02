package se.tink.backend.common.mapper;

import org.modelmapper.ModelMapper;
import se.tink.backend.core.product.ProductType;

public class CoreProductTypeMapper {
    public static se.tink.backend.aggregation.rpc.ProductType toAggregation(ProductType productType) {
        return new ModelMapper().map(productType, se.tink.backend.aggregation.rpc.ProductType.class);
    }
}
