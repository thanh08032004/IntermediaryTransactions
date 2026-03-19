package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.RoleRepository;
import hsf302.group3.intermediarytransactions.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }

    @GetMapping("/users/add")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String addUserForm(Model model) {
        User user = new User();
        user.setProfile(new hsf302.group3.intermediarytransactions.entity.UserProfile());
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/user-add";
    }

    @PostMapping("/users/save")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.createNewUser(user);
        return "redirect:/admin/users?success=added";
    }

    @GetMapping("/users/edit/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String editUserForm(@PathVariable Integer id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/user-edit";
    }

    @PostMapping("/users/update")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes ra,
                             Model model) {

        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/user-edit";
        }
        try {
            userService.updateUser(user);
            return "redirect:/admin/users?success=updated";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/edit/" + user.getId();
        }
    }

    @GetMapping("/users/delete/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=deleted";
    }

    @PostMapping("/users/toggle/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String toggleUserStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            return "redirect:/admin/users?success=status_changed";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}