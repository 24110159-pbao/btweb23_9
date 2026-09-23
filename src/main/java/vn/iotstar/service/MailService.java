package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.OtpType;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi OTP qua email.
     *
     * OTP plaintext chỉ tồn tại trong quá trình gửi email.
     * Không lưu plaintext vào database.
     */
    public void sendOtpEmail(
            String email,
            String otp,
            OtpType type
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email không được để trống"
            );
        }

        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP không được để trống"
            );
        }

        String subject;

        String action;

        if (type == OtpType.REGISTER) {

            subject = "Shop Management - Xác thực tài khoản";

            action = "xác thực tài khoản";

        } else if (type == OtpType.RESET_PASSWORD) {

            subject = "Shop Management - Đặt lại mật khẩu";

            action = "đặt lại mật khẩu";

        } else {

            throw new IllegalArgumentException(
                    "Loại OTP không được hỗ trợ"
            );
        }

        String text =
                """
                Xin chào,

                Bạn vừa yêu cầu %s trên hệ thống Shop Management.

                Mã OTP của bạn là:

                %s

                OTP có hiệu lực trong 5 phút.

                Bạn có tối đa 5 lần nhập OTP.

                Vì lý do bảo mật, vui lòng không chia sẻ mã OTP này
                với bất kỳ người nào.

                Nếu bạn không thực hiện yêu cầu này,
                vui lòng bỏ qua email này.

                Trân trọng,
                Shop Management
                """.formatted(
                        action,
                        otp
                );

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(subject);

        message.setText(text);

        mailSender.send(message);
    }
}
