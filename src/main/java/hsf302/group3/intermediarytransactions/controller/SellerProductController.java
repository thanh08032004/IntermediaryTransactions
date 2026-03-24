package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.ItemStatus;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductItem;
import hsf302.group3.intermediarytransactions.repository.ProductItemRepository;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.CategoryService;
import hsf302.group3.intermediarytransactions.service.ProductSellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/seller-products")
@PreAuthorize("hasRole('USER')")
public class SellerProductController {

    @Autowired
    private ProductSellerService productService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    ProductItemRepository productItemService;
    //  LIST
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") Integer page,
                       Authentication authentication, Model model) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer sellerId = user.getUser().getId();

        Page<Product> productPage = productService.getSellerProducts(
                sellerId, keyword, PageRequest.of(page, 5));

        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        return "user/product/seller-list";
    }

    //  CREATE FORM
    @GetMapping("/add")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        return "user/product/seller-add";
    }

    @PostMapping("/add")
    public String create(@ModelAttribute Product product,
                         @RequestParam(name = "mainIndex", required = false) Integer mainIndex,
                         Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer sellerId = user.getUser().getId();

        MultipartFile[] files = product.getUploadedFiles(); // Lấy từ @Transient

        productService.createWithImages(product, sellerId, files, mainIndex);

        return "redirect:/seller-products";
    }

    //  EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Authentication authentication, Model model) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer sellerId = user.getUser().getId();
        Product p = productService.getById(id, sellerId);

        model.addAttribute("product", p);
        model.addAttribute("categories", categoryService.getAll());

        return "user/product/seller-edit";
    }

    //  UPDATE
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Integer id,
            @ModelAttribute Product product,
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
            Authentication authentication
    ) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer sellerId = user.getUser().getId();

        MultipartFile[] files = product.getUploadedFiles();

        productService.update(id, product, sellerId, files, deleteImageIds);

        return "redirect:/seller-products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Integer sellerId = user.getUser().getId();
        productService.delete(id, sellerId);

        return "redirect:/seller-products";
    }
}