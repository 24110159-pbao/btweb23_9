package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.service.AuthService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String login(
            @RequestParam(
                    value = "error",
                    required = false
            ) String error,
            @RequestParam(
                    value = "logout",
                    required = false
            ) String logout,
            Authentication authentication,
            Model model
    ) {
        if (authentication != null
                && authentication.isAuthenticated()) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute(
                    "errorMessage",
                    "Username/email hoặc password không đúng"
            );
        }

        if (logout != null) {
            model.addAttribute(
                    "successMessage",
                    "Đăng xuất thành công"
            );
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute(
                    "registerDTO",
                    new RegisterDTO()
            );
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(dto);

            model.addAttribute(
                    "successMessage",
                    "Đăng ký thành công. OTP đã được gửi đến email."
            );

            VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO();
            verifyOtpDTO.setEmail(dto.getEmail());

            model.addAttribute(
                    "verifyOtpDTO",
                    verifyOtpDTO
            );

            return "auth/verify-otp";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpForm(
            @RequestParam(
                    value = "email",
                    required = false
            ) String email,
            Model model
    ) {
        VerifyOtpDTO dto = new VerifyOtpDTO();

        if (email != null && !email.isBlank()) {
            dto.setEmail(email);
        }

        model.addAttribute(
                "verifyOtpDTO",
                dto
        );

        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        try {
            authService.verifyRegisterOtp(dto);

            model.addAttribute(
                    "successMessage",
                    "Xác thực tài khoản thành công. Bạn có thể đăng nhập."
            );

            return "auth/login";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resendRegisterOtp(
            @RequestParam("email") String email,
            Model model
    ) {
        try {
            authService.resendRegisterOtp(email);

            VerifyOtpDTO dto = new VerifyOtpDTO();
            dto.setEmail(email);

            model.addAttribute(
                    "verifyOtpDTO",
                    dto
            );

            model.addAttribute(
                    "successMessage",
                    "OTP mới đã được gửi đến email."
            );

        } catch (IllegalArgumentException e) {

            VerifyOtpDTO dto = new VerifyOtpDTO();
            dto.setEmail(email);

            model.addAttribute(
                    "verifyOtpDTO",
                    dto
            );

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "auth/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute(
                    "forgotPasswordDTO",
                    new ForgotPasswordDTO()
            );
        }

        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotPasswordDTO")
            ForgotPasswordDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            authService.forgotPassword(dto);

            VerifyOtpDTO verifyOtpDTO = new VerifyOtpDTO();
            verifyOtpDTO.setEmail(dto.getEmail());

            model.addAttribute(
                    "verifyOtpDTO",
                    verifyOtpDTO
            );

            model.addAttribute(
                    "successMessage",
                    "OTP reset password đã được gửi đến email."
            );

            return "auth/reset-password";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(
            @RequestParam(
                    value = "email",
                    required = false
            ) String email,
            Model model
    ) {
        ResetPasswordDTO dto = new ResetPasswordDTO();

        if (email != null && !email.isBlank()) {
            dto.setEmail(email);
        }

        model.addAttribute(
                "resetPasswordDTO",
                dto
        );

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetPasswordDTO")
            ResetPasswordDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(dto);

            model.addAttribute(
                    "successMessage",
                    "Đặt lại password thành công. Bạn có thể đăng nhập."
            );

            return "auth/login";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "auth/reset-password";
        }
    }
}
