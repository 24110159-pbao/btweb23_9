package vn.iotstar.repository;

import vn.iotstar.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Lấy Product của một User.
     *
     * Dùng cho ROLE_USER.
     */
    Page<Product> findByUserId(
            Long userId,
            Pageable pageable
    );

    /**
     * Search Product của một User.
     *
     * Search:
     * - name
     * - description
     *
     * Dùng cho ROLE_USER.
     */
    @Query("""
            SELECT p
            FROM Product p
            WHERE p.user.id = :userId
              AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.description, ''))
                       LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY p.id DESC
            """)
    Page<Product> searchProductsByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Lấy toàn bộ Product.
     *
     * Dùng cho ROLE_ADMIN.
     */
    @Query("""
            SELECT p
            FROM Product p
            ORDER BY p.id DESC
            """)
    Page<Product> findAllProducts(Pageable pageable);

    /**
     * Search toàn bộ Product.
     *
     * Dùng cho ROLE_ADMIN.
     */
    @Query("""
            SELECT p
            FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(p.description, ''))
                  LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.id DESC
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Đếm tổng số Product.
     */
    @Query("""
            SELECT COUNT(p)
            FROM Product p
            """)
    long countProducts();

    /**
     * Đếm Product của một User.
     */
    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.user.id = :userId
            """)
    long countProductsByUserId(
            @Param("userId") Long userId
    );

    /**
     * Kiểm tra Product có thuộc User hay không.
     *
     * Dùng để bảo vệ các thao tác:
     * - update
     * - delete
     */
    boolean existsByIdAndUserId(
            Long productId,
            Long userId
    );
}
