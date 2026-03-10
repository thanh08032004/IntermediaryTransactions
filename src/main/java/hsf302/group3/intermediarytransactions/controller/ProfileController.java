package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.UserProfile;
import hsf302.group3.intermediarytransactions.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        if (user.getProfile() == null) {
            user.setProfile(new UserProfile());
        }
        model.addAttribute("user", user);
        return "profile/edit";
    }

//    @PostMapping("/update")
//    public String updateProfile(@ModelAttribute("user") User user, Principal principal) {
//        userService.updateMyProfile(principal.getName(), user.getProfile());
//        return "redirect:/profile?success";
//    }
@PostMapping("/update")
public String updateProfile(@ModelAttribute("user") User user,
                            BindingResult bindingResult,
                            Principal principal,
                            Model model) {

    if (bindingResult.hasErrors()) {
        System.out.println("Binding errors: " + bindingResult.getAllErrors());
        return "profile/edit";
    }

    try {
        userService.updateMyProfile(principal.getName(), user.getProfile());
        return "redirect:/profile?success";
    } catch (Exception e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "profile/edit";
    }
}
}