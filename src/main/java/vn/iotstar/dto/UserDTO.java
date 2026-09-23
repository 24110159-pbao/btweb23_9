package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3 đến 100 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @NotBlank(message = "Full name không được để trống")
    @Size(max = 150, message = "Full name không được vượt quá 150 ký tự")
    private String fullName;

    @NotBlank(message = "Role không được để trống")
    private String role;

    private boolean enabled;

    private long productCount;
}
