package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    // list products + phan trang + search
    @GetMapping
    public String getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<Product> productPage = productService.searchProducts(keyword, page, size);

        model.addAttribute("size", size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/product-list";
    }

    // add product (form)
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/add-product";
    }

    // save product
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product){
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    // edit product
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable int id, Model model){
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/edit-product";
    }

    // delete product
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable int id,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page){

        productService.deleteProduct(id);
        return "redirect:/admin/products?keyword=" + keyword + "&page=" + page;
    }

    @PostMapping("/toggle/{id}")
    public String toggleProduct(
            @PathVariable int id,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page){
        productService.toggleStatus(id);
        return "redirect:/admin/products?keyword=" + keyword + "&page=" + page;
    }
}