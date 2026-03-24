package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductItem;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.ProductItemService;
import hsf302.group3.intermediarytransactions.service.ProductSellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product-items")  // Tách ra khỏi /seller-products
public class ProductItemController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSellerService productService;

    @Autowired
    private ProductItemService productItemService;

    @GetMapping("/list/{productId}")
    public String viewProductItems(@PathVariable Integer productId, Model model) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("items", productItemService.findByProductId(productId));
        return "user/product/product-item";
    }

    // Xử lý submit
    @PostMapping("/add/{productId}")
    public String addItem(@PathVariable Integer productId,
                          @ModelAttribute ProductItem productItem) {
        productItemService.addItem(productId, productItem);
        return "redirect:/product-items/list/" + productId;
    }
    @GetMapping("/add/{productId}")
    public String addItemForm(@PathVariable Integer productId, Model model) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductItem item = new ProductItem();
        model.addAttribute("productItem", item);
        model.addAttribute("product", product);

        return "user/product/product-additem";
    }

}