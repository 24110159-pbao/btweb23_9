package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(
            String keyword,
            Pageable pageable
    ) {
        Page<User> users;

        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAllUsers(pageable);
        } else {
            users = userRepository.searchUsers(
                    keyword.trim(),
                    pageable
            );
        }

        return users.map(userMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy User"));

        return userMapper.toDTO(user);
    }

    public UserDTO createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() ->
                        new IllegalArgumentException("Role không tồn tại"));

        User user = userMapper.toEntity(dto);

        user.setRole(role);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setEnabled(true);

        return userMapper.toDTO(
                userRepository.save(user)
        );
    }

    public UserDTO updateUser(
            Long id,
            UserDTO dto
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy User"));

        if (userRepository.existsByUsername(dto.getUsername())
                && !user.getUsername().equals(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(dto.getEmail())
                && !user.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() ->
                        new IllegalArgumentException("Role không tồn tại"));

        userMapper.updateEntity(dto, user);
        user.setRole(role);

        return userMapper.toDTO(
                userRepository.save(user)
        );
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy User"));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.countUsers();
    }

    @Transactional(readOnly = true)
    public long countProductsByUserId(Long userId) {
        return userRepository.countProductsByUserId(userId);
    }
}
