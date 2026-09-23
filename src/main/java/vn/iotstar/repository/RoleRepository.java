package vn.iotstar.repository;

import vn.iotstar.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm Role theo tên.
     *
     * Ví dụ:
     * ROLE_USER
     * ROLE_ADMIN
     */
    Optional<Role> findByName(String name);

    /**
     * Kiểm tra Role đã tồn tại.
     */
    boolean existsByName(String name);
}
