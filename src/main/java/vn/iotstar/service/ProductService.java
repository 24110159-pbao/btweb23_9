package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(
            String keyword,
            Pageable pageable,
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy User"));

        Page<Product> products;

        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRepository.findByUserId(
                    user.getId(),
                    pageable
            );
        } else {
            products = productRepository.searchProductsByUser(
                    user.getId(),
                    keyword.trim(),
                    pageable
            );
        }

        return products.map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(
            String keyword,
            Pageable pageable
    ) {
        Page<Product> products;

        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRepository.findAllProducts(pageable);
        } else {
            products = productRepository.searchProducts(
                    keyword.trim(),
                    pageable
            );
        }

        return products.map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(
            Long id,
            String username
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        if (!product.getUser().getUsername().equals(username)) {
            throw new SecurityException(
                    "Bạn không có quyền xem Product này"
            );
        }

        return productMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductByIdForAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        return productMapper.toDTO(product);
    }

    public ProductDTO createProduct(
            ProductDTO dto,
            MultipartFile image,
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy User"));

        validateProduct(dto);

        Product product = productMapper.toEntity(dto);
        product.setUser(user);

        uploadImage(product, image);

        return productMapper.toDTO(
                productRepository.save(product)
        );
    }

    public ProductDTO updateProduct(
            Long id,
            ProductDTO dto,
            MultipartFile image,
            String username
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        if (!product.getUser().getUsername().equals(username)) {
            throw new SecurityException(
                    "Bạn không có quyền sửa Product này"
            );
        }

        validateProduct(dto);

        String oldPublicId = product.getImagePublicId();

        productMapper.updateEntity(dto, product);

        replaceImage(
                product,
                image,
                oldPublicId
        );

        return productMapper.toDTO(
                productRepository.save(product)
        );
    }

    public ProductDTO updateProductForAdmin(
            Long id,
            ProductDTO dto,
            MultipartFile image
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        validateProduct(dto);

        String oldPublicId = product.getImagePublicId();

        productMapper.updateEntity(dto, product);

        replaceImage(
                product,
                image,
                oldPublicId
        );

        return productMapper.toDTO(
                productRepository.save(product)
        );
    }

    public void deleteProduct(
            Long id,
            String username
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        if (!product.getUser().getUsername().equals(username)) {
            throw new SecurityException(
                    "Bạn không có quyền xóa Product này"
            );
        }

        deleteProductImage(product);

        productRepository.delete(product);
    }

    public void deleteProductForAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy Product"));

        deleteProductImage(product);

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.countProducts();
    }

    @Transactional(readOnly = true)
    public long countProductsByUserId(Long userId) {
        return productRepository.countProductsByUserId(userId);
    }

    private void validateProduct(ProductDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Tên Product không được để trống"
            );
        }

        if (dto.getPrice() == null) {
            throw new IllegalArgumentException(
                    "Giá Product không được để trống"
            );
        }

        if (dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá Product không hợp lệ"
            );
        }
    }

    private void uploadImage(
            Product product,
            MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            return;
        }

        Map<String, Object> result =
                cloudinaryService.upload(image);

        product.setImageUrl(
                (String) result.get("secure_url")
        );

        product.setImagePublicId(
                (String) result.get("public_id")
        );
    }

    private void replaceImage(
            Product product,
            MultipartFile image,
            String oldPublicId
    ) {
        if (image == null || image.isEmpty()) {
            return;
        }

        Map<String, Object> result =
                cloudinaryService.upload(image);

        product.setImageUrl(
                (String) result.get("secure_url")
        );

        product.setImagePublicId(
                (String) result.get("public_id")
        );

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            cloudinaryService.delete(oldPublicId);
        }
    }

    private void deleteProductImage(Product product) {
        if (product.getImagePublicId() != null
                && !product.getImagePublicId().isBlank()) {
            cloudinaryService.delete(
                    product.getImagePublicId()
            );
        }
    }
}
