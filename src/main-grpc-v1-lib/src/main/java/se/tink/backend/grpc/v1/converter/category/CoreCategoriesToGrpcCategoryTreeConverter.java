package se.tink.backend.grpc.v1.converter.category;

import com.google.common.collect.Lists;
import se.tink.backend.core.Category;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.CategoryTreeBuilder;

public class CoreCategoriesToGrpcCategoryTreeConverter
        implements Converter<Iterable<Category>, se.tink.grpc.v1.models.CategoryTree> {
    private final CoreCategoryNodeToGrpcCategoryNodeConverter categoryNodeToGrpcConverter = new CoreCategoryNodeToGrpcCategoryNodeConverter();

    @Override
    public se.tink.grpc.v1.models.CategoryTree convertFrom(Iterable<Category> categories) {
        CategoryTree categoryTree = new CategoryTreeBuilder().addNodes(Lists.newArrayList(categories)).build();

        se.tink.grpc.v1.models.CategoryTree.Builder builder = se.tink.grpc.v1.models.CategoryTree.newBuilder();
        ConverterUtils.setIfPresent(categoryTree::getExpenses, builder::setExpenses,
                categoryNodeToGrpcConverter::convertFrom);
        ConverterUtils
                .setIfPresent(categoryTree::getIncome, builder::setIncome, categoryNodeToGrpcConverter::convertFrom);
        ConverterUtils.setIfPresent(categoryTree::getTransfers, builder::setTransfers,
                categoryNodeToGrpcConverter::convertFrom);
        return builder.build();
    }
}
