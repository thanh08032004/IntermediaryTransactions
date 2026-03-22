package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.ProductItem;

import java.util.List;

public interface ProductItemService {

    List<ProductItem> findByProductId(Integer productId);

    List<ProductItem> getAvailableItems(Integer productId);

    ProductItem save(ProductItem item);

    void delete(Integer id);
}