package se.tink.backend.grpc.v1.converter.category;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;

public class CoreCategoryNodeToGrpcCategoryNodeConverter
        implements Converter<CategoryNode, se.tink.grpc.v1.models.CategoryNode> {
    @Override
    public se.tink.grpc.v1.models.CategoryNode convertFrom(CategoryNode categoryNode) {
        se.tink.grpc.v1.models.CategoryNode.Builder builder = se.tink.grpc.v1.models.CategoryNode.newBuilder();
        ConverterUtils.setIfPresent(categoryNode::getCode, builder::setCode);
        ConverterUtils.setIfPresent(categoryNode::getId, builder::setId);
        ConverterUtils.setIfPresent(categoryNode::getName, builder::setName);
        ConverterUtils.setIfPresent(categoryNode::getSortOrder, builder::setSortOrder);
        ConverterUtils.setIfPresent(categoryNode::isDefaultChild, builder::setDefaultChild);
        builder.addAllChildren(convertChildren(categoryNode.getChildren()));
        return builder.build();
    }

    private List<se.tink.grpc.v1.models.CategoryNode> convertChildren(
            Map<String, CategoryNode> children) {
        List<se.tink.grpc.v1.models.CategoryNode> categoryNodes = Lists.newArrayListWithCapacity(children.size());
        for (CategoryNode child : children.values()) {
            categoryNodes.add(convertFrom(child));
        }

        return categoryNodes;
    }
}
