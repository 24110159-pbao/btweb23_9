package vn.iotstar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security gọi method này khi đăng nhập.
     *
     * Người dùng có thể đăng nhập bằng:
     * - Username
     * - Email
     *
     * Role được JOIN FETCH cùng User để tránh lỗi:
     *
     * Could not initialize proxy [Role#...] - no session
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        if (usernameOrEmail == null
                || usernameOrEmail.isBlank()) {

            throw new UsernameNotFoundException(
                    "Username hoặc email không được để trống"
            );
        }

        String loginValue = usernameOrEmail.trim();

        User user = userRepository
                .findByUsernameOrEmailWithRole(
                        loginValue,
                        loginValue
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Username hoặc email không tồn tại"
                        )
                );

        return new CustomUserDetails(user);
    }
}
