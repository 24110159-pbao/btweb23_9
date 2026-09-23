package vn.iotstar.repository;

import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /**
     * Tìm OTP mới nhất chưa sử dụng.
     *
     * Dùng cho:
     * - REGISTER
     * - RESET_PASSWORD
     */
    Optional<OtpToken> findFirstByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email,
            OtpType type
    );

    /**
     * Tìm OTP mới nhất theo email + type.
     */
    Optional<OtpToken> findFirstByEmailAndTypeOrderByCreatedAtDesc(
            String email,
            OtpType type
    );

    /**
     * Kiểm tra có OTP chưa sử dụng hay không.
     */
    boolean existsByEmailAndTypeAndUsedFalse(
            String email,
            OtpType type
    );

    /**
     * Vô hiệu hóa các OTP cũ.
     *
     * Khi gửi OTP mới:
     * - OTP cũ sẽ được đánh dấu used = true
     * - OTP mới trở thành OTP duy nhất có thể verify.
     */
    @Modifying
    @Query("""
            UPDATE OtpToken o
            SET o.used = true
            WHERE o.email = :email
              AND o.type = :type
              AND o.used = false
            """)
    int invalidateUnusedOtps(
            @Param("email") String email,
            @Param("type") OtpType type
    );

    /**
     * Xóa các OTP đã hết hạn trước một thời điểm.
     *
     * Có thể dùng để cleanup dữ liệu OTP.
     */
    @Modifying
    @Query("""
            DELETE FROM OtpToken o
            WHERE o.expiresAt < :dateTime
            """)
    int deleteExpiredOtps(
            @Param("dateTime") LocalDateTime dateTime
    );

    /**
     * Xóa OTP cũ của email + type.
     */
    @Modifying
    @Query("""
            DELETE FROM OtpToken o
            WHERE o.email = :email
              AND o.type = :type
            """)
    int deleteByEmailAndType(
            @Param("email") String email,
            @Param("type") OtpType type
    );
}
