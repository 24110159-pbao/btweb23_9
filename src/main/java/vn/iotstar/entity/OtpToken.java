package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "otp_tokens",
        indexes = {
                @Index(
                        name = "idx_otp_email_type_used",
                        columnList = "email, type, used"
                ),
                @Index(
                        name = "idx_otp_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email dùng để xác định tài khoản cần verify/reset.
     *
     * Chỉ lưu email đã normalize về lowercase.
     */
    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    /**
     * BCrypt hash của OTP.
     *
     * Tuyệt đối không lưu OTP plaintext.
     */
    @Column(
            name = "otp_hash",
            nullable = false,
            length = 255
    )
    private String otpHash;

    /**
     * REGISTER:
     *     Xác thực tài khoản.
     *
     * RESET_PASSWORD:
     *     Reset password.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 30
    )
    private OtpType type;

    /**
     * Thời điểm OTP hết hạn.
     *
     * Theo yêu cầu:
     * OTP có hiệu lực 5 phút.
     */
    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    /**
     * OTP đã được sử dụng hay chưa.
     *
     * false = còn hiệu lực về mặt trạng thái sử dụng
     * true  = đã sử dụng hoặc đã bị vô hiệu hóa
     */
    @Column(
            name = "used",
            nullable = false
    )
    private boolean used = false;

    /**
     * Số lần nhập OTP sai.
     *
     * Tối đa 5 lần.
     */
    @Column(
            name = "attempts",
            nullable = false
    )
    private int attempts = 0;

    /**
     * Thời điểm tạo OTP.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public OtpToken(
            String email,
            String otpHash,
            OtpType type,
            LocalDateTime expiresAt
    ) {
        this.email = email;
        this.otpHash = otpHash;
        this.type = type;
        this.expiresAt = expiresAt;
        this.used = false;
        this.attempts = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
