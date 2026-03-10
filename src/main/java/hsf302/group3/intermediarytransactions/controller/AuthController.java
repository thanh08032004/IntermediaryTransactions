package hsf302.group3.intermediarytransactions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
class AuthController {
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
