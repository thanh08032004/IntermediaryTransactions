package hsf302.group3.intermediarytransactions.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModel {

    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
    }
}