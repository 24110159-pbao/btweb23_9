package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(
                        name = "idx_product_name",
                        columnList = "name"
                ),
                @Index(
                        name = "idx_product_user_id",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 200
    )
    private String name;

    @Column(
            name = "description",
            length = 5000
    )
    private String description;

    @Column(
            name = "price",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal price;

    @Column(
            name = "image_url",
            length = 1000
    )
    private String imageUrl;

    @Column(
            name = "image_public_id",
            length = 500
    )
    private String imagePublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_user"
            )
    )
    private User user;

    public Product(
            String name,
            String description,
            BigDecimal price,
            User user
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.user = user;
    }
}
