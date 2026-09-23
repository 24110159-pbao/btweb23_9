package vn.iotstar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Tên Product không được để trống")
    @Size(
            max = 200,
            message = "Tên Product không được vượt quá 200 ký tự"
    )
    private String name;

    @Size(
            max = 5000,
            message = "Description không được vượt quá 5000 ký tự"
    )
    private String description;

    @NotNull(message = "Giá Product không được để trống")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Giá Product phải lớn hơn hoặc bằng 0"
    )
    private BigDecimal price;

    private String imageUrl;

    private String imagePublicId;

    private String username;
}
