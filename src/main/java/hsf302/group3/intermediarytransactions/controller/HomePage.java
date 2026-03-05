package hsf302.group3.intermediarytransactions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePage {
    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }
}
