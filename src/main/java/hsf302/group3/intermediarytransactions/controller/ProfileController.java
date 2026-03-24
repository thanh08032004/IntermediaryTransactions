package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.dto.UserProfileDto;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.UserProfile;
import hsf302.group3.intermediarytransactions.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        UserProfile profile = user.getProfile();

        UserProfileDto dto = new UserProfileDto();
        if (profile != null) {
            dto.setFullname(profile.getFullname());
            dto.setPhone(profile.getPhone());
            dto.setEmail(profile.getEmail());
            dto.setGender(profile.getGender());
            dto.setAvatar(profile.getAvatar());
            dto.setDateOfBirth(profile.getDateOfBirth());
            dto.setDescription(profile.getDescription());
        }

        model.addAttribute("profile", dto);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDto dto,
                                BindingResult result,
                                Principal principal,
                                Model model) {

        if (result.hasErrors()) {
            return "profile/edit";
        }

        try {
            userService.updateMyProfile(principal.getName(), dto);
            return "redirect:/profile?success";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "profile/edit";
        }
    }
}