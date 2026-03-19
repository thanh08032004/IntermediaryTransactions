package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.service.ProductAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import hsf302.group3.intermediarytransactions.entity.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductAdminService productAdminService;

    // list products + phan trang + search
    @GetMapping
    public String getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<Product> productPage = productAdminService.searchProducts(keyword, page, size);

        model.addAttribute("size", size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "market";
    }
    // update product
    @PostMapping("/update")
    public String updateProduct(
            @Valid @ModelAttribute Product product,
            BindingResult result,
            Model model,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "mainIndex", required = false) Integer mainIndex,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page
    ) {

        if (result.hasErrors()) {
            model.addAttribute("categories", productAdminService.getAllCategories());
            return "admin/product-edit";
        }

        productAdminService.updateProductByAdmin(product, images, mainIndex);

        return "redirect:/admin/products?success=updated&keyword=" + keyword + "&page=" + page;
    }

    // add product (form)
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productAdminService.getAllCategories());
        return "admin/product-add";
    }
    @PostMapping("/add")
    public String addProduct(
            @Valid @ModelAttribute Product product,
            BindingResult result,
            Model model,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "mainIndex", required = false) Integer mainIndex
    ) {

        if (result.hasErrors()) {
            model.addAttribute("categories", productAdminService.getAllCategories());
            return "admin/product-add";
        }

        productAdminService.saveProductWithImages(product, images, mainIndex);

        return "redirect:/admin/products?success=added";
    }

    // edit product
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable int id,
                              @RequestParam(defaultValue = "") String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              Model model){

        Product product = productAdminService.getProductById(id);

        if (product == null) {
            return "redirect:/admin/products?error=notfound";
        }

        if (product.getCategory() == null) {
            product.setCategory(new Category());
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", productAdminService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);

        return "admin/product-edit";
    }

    // delete product
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable int id,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page){

        productAdminService.deleteProduct(id);
        return "redirect:/admin/products?success=deleted&keyword=" + keyword + "&page=" + page;
    }

    @PostMapping("/toggle/{id}")
    public String toggleProduct(
            @PathVariable int id,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page){
        productAdminService.toggleStatus(id);
        return "redirect:/admin/products?keyword=" + keyword + "&page=" + page;
    }
}