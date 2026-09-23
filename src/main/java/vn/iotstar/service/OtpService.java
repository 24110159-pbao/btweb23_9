package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.repository.OtpTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private static final int OTP_EXPIRATION_MINUTES = 5;

    private static final int MAX_ATTEMPTS = 5;

    private final OtpTokenRepository otpTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Tạo OTP mới, hash OTP rồi lưu DB và gửi email.
     *
     * Quy trình:
     * 1. Vô hiệu hóa OTP cũ.
     * 2. Sinh OTP 6 số.
     * 3. Hash OTP bằng BCrypt.
     * 4. Lưu hash vào DB.
     * 5. Gửi OTP plaintext qua email.
     */
    public void sendOtp(
            String email,
            OtpType type
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        String normalizedEmail = email.trim().toLowerCase();

        /*
         * OTP cũ không còn hiệu lực.
         */
        otpTokenRepository.invalidateUnusedOtps(
                normalizedEmail,
                type
        );

        /*
         * Sinh OTP 6 số.
         */
        String otp = generateOtp();

        /*
         * Chỉ lưu hash.
         *
         * Không bao giờ lưu OTP plaintext vào DB.
         */
        String otpHash = passwordEncoder.encode(otp);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(OTP_EXPIRATION_MINUTES);

        OtpToken otpToken = new OtpToken(
                normalizedEmail,
                otpHash,
                type,
                expiresAt
        );

        otpTokenRepository.save(otpToken);

        /*
         * Chỉ OTP plaintext được gửi qua email.
         * DB chỉ chứa BCrypt hash.
         */
        emailService.sendOtpEmail(
                normalizedEmail,
                otp,
                type
        );
    }

    /**
     * Verify OTP.
     *
     * Quy tắc:
     * - Chỉ lấy OTP mới nhất chưa sử dụng.
     * - Hết hạn => không hợp lệ.
     * - Sai quá 5 lần => OTP bị vô hiệu hóa.
     * - Đúng => đánh dấu used=true.
     *
     * @return true nếu OTP hợp lệ.
     */
    public boolean verifyOtp(
            String email,
            OtpType type,
            String otp
    ) {
        if (email == null || email.isBlank()) {
            return false;
        }

        if (otp == null || otp.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        String normalizedOtp = otp.trim();

        /*
         * OTP bắt buộc phải đúng 6 chữ số.
         */
        if (!normalizedOtp.matches("\\d{6}")) {
            return false;
        }

        OtpToken otpToken =
                otpTokenRepository
                        .findFirstByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
                                normalizedEmail,
                                type
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OTP không tồn tại hoặc đã được sử dụng"
                                )
                        );

        /*
         * Kiểm tra OTP đã hết hạn.
         */
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            otpToken.setUsed(true);

            otpTokenRepository.save(otpToken);

            throw new IllegalArgumentException(
                    "OTP đã hết hạn"
            );
        }

        /*
         * Kiểm tra số lần thử.
         */
        if (otpToken.getAttempts() >= MAX_ATTEMPTS) {

            otpToken.setUsed(true);

            otpTokenRepository.save(otpToken);

            throw new IllegalArgumentException(
                    "OTP đã vượt quá số lần thử cho phép"
            );
        }

        /*
         * So sánh OTP plaintext với BCrypt hash.
         */
        boolean matches =
                passwordEncoder.matches(
                        normalizedOtp,
                        otpToken.getOtpHash()
                );

        if (!matches) {

            int attempts = otpToken.getAttempts() + 1;

            otpToken.setAttempts(attempts);

            /*
             * Nếu đã sai đủ 5 lần thì vô hiệu hóa OTP.
             */
            if (attempts >= MAX_ATTEMPTS) {
                otpToken.setUsed(true);
            }

            otpTokenRepository.save(otpToken);

            if (attempts >= MAX_ATTEMPTS) {
                throw new IllegalArgumentException(
                        "OTP không hợp lệ. Bạn đã vượt quá 5 lần thử."
                );
            }

            throw new IllegalArgumentException(
                    "OTP không hợp lệ. Còn "
                            + (MAX_ATTEMPTS - attempts)
                            + " lần thử."
            );
        }

        /*
         * OTP đúng.
         *
         * Đánh dấu used=true để không thể dùng lại.
         */
        otpToken.setUsed(true);

        otpTokenRepository.save(otpToken);

        return true;
    }

    /**
     * Sinh OTP đúng 6 chữ số.
     *
     * Ví dụ:
     * 004821
     * 123456
     * 900015
     */
    private String generateOtp() {
        int otpNumber = secureRandom.nextInt(1_000_000);

        return String.format(
                "%0" + OTP_LENGTH + "d",
                otpNumber
        );
    }
}
