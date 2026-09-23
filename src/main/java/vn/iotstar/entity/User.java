package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_user_email",
                        columnNames = "email"
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_username",
                        columnList = "username"
                ),
                @Index(
                        name = "idx_user_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_user_full_name",
                        columnList = "full_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_user_role"
            )
    )
    private Role role;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Product> products = new ArrayList<>();

    public User(
            String username,
            String email,
            String password,
            String fullName,
            boolean enabled,
            Role role
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.enabled = enabled;
        this.role = role;
    }

    public void addProduct(Product product) {
        if (product == null) {
            return;
        }

        products.add(product);
        product.setUser(this);
    }

    public void removeProduct(Product product) {
        if (product == null) {
            return;
        }

        products.remove(product);
        product.setUser(null);
    }
}
