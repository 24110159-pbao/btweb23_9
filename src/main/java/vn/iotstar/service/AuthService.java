package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER không tồn tại"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole(role);
        user.setEnabled(false);

        userRepository.save(user);

        otpService.sendOtp(
                user.getEmail(),
                OtpType.REGISTER
        );
    }

    public void verifyRegisterOtp(VerifyOtpDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực");
        }

        boolean valid = otpService.verifyOtp(
                dto.getEmail(),
                OtpType.REGISTER,
                dto.getOtp()
        );

        if (!valid) {
            throw new IllegalArgumentException("OTP không hợp lệ");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    public void resendRegisterOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực");
        }

        otpService.sendOtp(
                email,
                OtpType.REGISTER
        );
    }

    public void forgotPassword(ForgotPasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Email không tồn tại"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt");
        }

        otpService.sendOtp(
                user.getEmail(),
                OtpType.RESET_PASSWORD
        );
    }

    public void verifyResetPasswordOtp(VerifyOtpDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt");
        }

        boolean valid = otpService.verifyOtp(
                dto.getEmail(),
                OtpType.RESET_PASSWORD,
                dto.getOtp()
        );

        if (!valid) {
            throw new IllegalArgumentException("OTP không hợp lệ");
        }
    }

    public void resetPassword(ResetPasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản chưa được kích hoạt");
        }

        boolean valid = otpService.verifyOtp(
                dto.getEmail(),
                OtpType.RESET_PASSWORD,
                dto.getOtp()
        );

        if (!valid) {
            throw new IllegalArgumentException("OTP không hợp lệ");
        }

        user.setPassword(
                passwordEncoder.encode(dto.getNewPassword())
        );

        userRepository.save(user);
    }
}
