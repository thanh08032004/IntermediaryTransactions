package hsf302.group3.intermediarytransactions.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Lấy redirect từ query param ưu tiên
        String redirectUrl = request.getParameter("redirect");
        if(redirectUrl != null && !redirectUrl.isEmpty()){
            response.sendRedirect(redirectUrl);
            return;
        }

        // fallback: nếu có session redirectAfterLogin (cũ)
        HttpSession session = request.getSession(false);
        if (session != null) {
            redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirectUrl);
                return;
            }
        }

        // default nếu không có redirect
        response.sendRedirect("/");
    }
}