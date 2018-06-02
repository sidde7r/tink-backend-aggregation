package se.tink.backend.grpc.v1.utils;

import java.util.List;
import se.tink.backend.core.Category;
import se.tink.backend.grpc.v1.converter.category.CategoryNode;
import se.tink.backend.grpc.v1.converter.category.CategoryTree;

public class CategoryTreeBuilder {
    private final CategoryNode root;

    public CategoryTreeBuilder() {
        this.root = new CategoryNode();
    }

    public CategoryTreeBuilder addNodes(List<Category> categories) {
        for (Category category : categories) {
            addNode(category);
        }
        return this;
    }

    public CategoryTreeBuilder addNode(Category category) {
        return addNode(convert(category));
    }

    public CategoryTreeBuilder addNode(CategoryNode categoryNode) {
        return addNode(root, categoryNode, 0);
    }

    public CategoryTree build() {
        CategoryTree tree = new CategoryTree();
        tree.setExpenses(root.getChildren().get("expenses"));
        tree.setIncome(root.getChildren().get("income"));
        tree.setTransfers(root.getChildren().get("transfers"));
        return tree;
    }

    private CategoryTreeBuilder addNode(CategoryNode ancestorNode, CategoryNode descendantNode, int level) {
        String levelKey = descendantNode.getCodeByLevel(level);
        if (descendantNode.getLevel() == level) {
            addOrUpdateNode(ancestorNode, descendantNode, levelKey);
            return this;
        }

        CategoryNode nextAncestor = ancestorNode.getChildren().get(levelKey);
        if (nextAncestor == null) {
            nextAncestor = new CategoryNode();
            ancestorNode.putChild(levelKey, nextAncestor);
        }

        addNode(nextAncestor, descendantNode, level + 1);
        return this;
    }

    private void addOrUpdateNode(CategoryNode parentNode, CategoryNode childNode, String levelKey) {
        CategoryNode existedChild = parentNode.getChildren().get(levelKey);
        if (existedChild != null) {
            childNode.setChildren(existedChild.getChildren());
        }
        parentNode.putChild(levelKey, childNode);
    }

    private CategoryNode convert(Category category) {
        CategoryNode categoryNode = new CategoryNode();
        categoryNode.setCode(category.getCode());
        categoryNode.setId(category.getId());
        categoryNode.setName(category.getDisplayName());
        categoryNode.setSortOrder(category.getSortOrder());
        categoryNode.setDefaultChild(category.isDefaultChild());

        return categoryNode;
    }
}
