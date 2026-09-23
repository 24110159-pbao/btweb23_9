package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm User theo username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm User theo email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại hay chưa.
     */
    boolean existsByUsername(String username);

    /**
     * Kiểm tra email đã tồn tại hay chưa.
     */
    boolean existsByEmail(String email);

    /**
     * Tìm User theo username hoặc email.
     *
     * Dùng cho các trường hợp thông thường.
     */
    Optional<User> findByUsernameOrEmail(
            String username,
            String email
    );

    /**
     * Tìm User theo username hoặc email
     * và load Role ngay trong cùng query.
     *
     * Cần dùng cho Spring Security login vì
     * User.role đang dùng FetchType.LAZY.
     */
    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.role
            WHERE u.username = :username
               OR u.email = :email
            """)
    Optional<User> findByUsernameOrEmailWithRole(
            @Param("username") String username,
            @Param("email") String email
    );

    /**
     * Search User theo:
     * - username
     * - email
     * - fullName
     *
     * Có hỗ trợ pagination.
     */
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY u.id DESC
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Lấy toàn bộ User có pagination.
     */
    @Query("""
            SELECT u
            FROM User u
            ORDER BY u.id DESC
            """)
    Page<User> findAllUsers(Pageable pageable);

    /**
     * Đếm tổng số User.
     */
    @Query("""
            SELECT COUNT(u)
            FROM User u
            """)
    long countUsers();

    /**
     * Đếm số Product của một User.
     */
    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.user.id = :userId
            """)
    long countProductsByUserId(
            @Param("userId") Long userId
    );
}
