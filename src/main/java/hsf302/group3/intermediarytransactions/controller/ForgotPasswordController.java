package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public ForgotPasswordController(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String input, RedirectAttributes ra) {
        // 1. Tìm User dựa trên cái bạn nhập vào ô input (có thể là username 'customer2')
        Optional<User> userOpt = userRepository.findByUsername(input);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 2. Lấy Email thật từ bảng Profile (tranducthangbk2k5@gmail.com)
            if (user.getProfile() != null && user.getProfile().getEmail() != null) {
                String targetEmail = user.getProfile().getEmail();

                // Logic tạo token...
                String token = UUID.randomUUID().toString();
                user.setResetToken(token);
                userRepository.save(user);

                // Tạo nội dung HTML cho Email
                String resetLink = "http://localhost:8080/reset-password?token=" + token;

                String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                        "    <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
                        "        <h1 style='color: white; margin: 0;'>HSF System</h1>" +
                        "    </div>" +
                        "    <div style='padding: 30px; color: #333; line-height: 1.6;'>" +
                        "        <h2 style='color: #764ba2;'>Yêu cầu đặt lại mật khẩu</h2>" +
                        "        <p>Chào bạn,</p>" +
                        "        <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhấn vào nút bên dưới để tiến hành thay đổi:</p>" +
                        "        <div style='text-align: center; margin: 30px 0;'>" +
                        "            <a href='" + resetLink + "' style='background-color: #764ba2; color: white; padding: 15px 25px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;'>ĐẶT LẠI MẬT KHẨU</a>" +
                        "        </div>" +
                        "        <p>Nếu nút trên không hoạt động, bạn có thể copy link này dán vào trình duyệt:</p>" +
                        "        <p style='word-break: break-all; color: #667eea;'>" + resetLink + "</p>" +
                        "        <p style='border-top: 1px solid #eee; pt-20; font-size: 12px; color: #888;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>" +
                        "    </div>" +
                        "</div>";
                try {
                    // 3. GỬI ĐẾN targetEmail (Email thật), KHÔNG GỬI ĐẾN input (username)
                    emailService.sendEmail(targetEmail, "Reset Password Request", content);

                    ra.addFlashAttribute("message", "Link reset đã được gửi đến email đăng ký của bạn.");
                } catch (MessagingException e) {
                    ra.addFlashAttribute("error", "Lỗi gửi mail: " + e.getMessage());
                }
            } else {
                ra.addFlashAttribute("error", "Tài khoản này chưa có thông tin email trong Profile!");
            }
        } else {
            ra.addFlashAttribute("error", "Tài khoản không tồn tại!");
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<User> user = userRepository.findByResetToken(token);
        if (user.isEmpty()) {
            return "redirect:/login?error=invalid_token";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      RedirectAttributes ra) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            userRepository.save(user);
            ra.addFlashAttribute("message", "Mật khẩu đã được cập nhật thành công!");
        }
        return "redirect:/login";
    }
}