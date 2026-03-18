package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.service.CategoryService;
import hsf302.group3.intermediarytransactions.service.ProductBuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Controller
@RequestMapping("/buyer-products")
public class BuyerProductController {

    @Autowired
    private ProductBuyerService productBuyerService;
    @Autowired
    private CategoryService categoryService;

    // 1. LIST
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 5);

        Page<Product> productPage = productBuyerService.searchProducts(
                keyword, categoryId, pageable);

        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.getAll());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "user/product/buyer-list";
    }

    // 2. DETAIL
    @GetMapping("/view/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        Product product = productBuyerService.getProductById(id);

        model.addAttribute("product", product);
        return "user/product/buyer-detail";
    }
}