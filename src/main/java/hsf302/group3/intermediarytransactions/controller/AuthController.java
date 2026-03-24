package hsf302.group3.intermediarytransactions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model
    ) {
        // Nếu có redirect, truyền vào view để đặt hidden input
        if (redirect != null && !redirect.isBlank()) {
            model.addAttribute("redirectUrl", redirect);
        }
        return "login"; // template login.html
    }
}