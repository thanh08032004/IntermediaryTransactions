package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Category;
import hsf302.group3.intermediarytransactions.repository.CategoryRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import hsf302.group3.intermediarytransactions.util.constant.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryController(CategoryRepository categoryRepository,
                              ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Category> categoryPage;

        if (keyword != null && !keyword.isEmpty()) {
            categoryPage = categoryRepository
                    .findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/category-list";
    }

    @GetMapping("/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-add";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories?success=added";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Integer id, Model model) {
        model.addAttribute("category", categoryRepository.findById(id).orElseThrow());
        return "admin/category-edit";
    }

    @PostMapping("/update")
    public String updateCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories?success=updated";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id) {

        Category category = categoryRepository.findById(id).orElseThrow();

        if (category.getStatus() == CategoryStatus.ACTIVE) {
            category.setStatus(CategoryStatus.INACTIVE);
        } else {
            category.setStatus(CategoryStatus.ACTIVE);
        }

        categoryRepository.save(category);

        return "redirect:/admin/categories?success=status_changed";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        if(productRepository.existsByCategoryId(id)){
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete category because it is used by products.");
            return "redirect:/admin/categories";
        }

        categoryRepository.deleteById(id);

        return "redirect:/admin/categories?success=deleted";
    }
}
