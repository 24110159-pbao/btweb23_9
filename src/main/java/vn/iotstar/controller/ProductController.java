package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "id"
                )
        );

        Page<ProductDTO> productPage;

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals("ROLE_ADMIN")
                        );

        if (isAdmin) {
            productPage =
                    productService.getAllProducts(
                            keyword,
                            pageable
                    );
        } else {
            productPage =
                    productService.getProducts(
                            keyword,
                            pageable,
                            authentication.getName()
                    );
        }

        model.addAttribute(
                "products",
                productPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                productPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                productPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                productPage.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "isAdmin",
                isAdmin
        );

        model.addAttribute(
                "totalProducts",
                productService.countProducts()
        );

        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(
            Model model
    ) {
        if (!model.containsAttribute("productDTO")) {
            model.addAttribute(
                    "productDTO",
                    new ProductDTO()
            );
        }

        model.addAttribute(
                "formTitle",
                "Thêm Product"
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "products/form";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("productDTO") ProductDTO dto,
            @RequestParam(value = "image", required = false)
            MultipartFile image,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.createProduct(
                    dto,
                    image,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm Product thành công."
            );

            return "redirect:/products";

        } catch (IllegalArgumentException | SecurityException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "productDTO",
                    dto
            );

            return "redirect:/products/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean isAdmin =
                    authentication.getAuthorities()
                            .stream()
                            .anyMatch(
                                    authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_ADMIN")
                            );

            ProductDTO dto;

            if (isAdmin) {
                dto = productService.getProductByIdForAdmin(id);
            } else {
                dto = productService.getProductById(
                        id,
                        authentication.getName()
                );
            }

            model.addAttribute(
                    "productDTO",
                    dto
            );

            model.addAttribute(
                    "formTitle",
                    "Chỉnh sửa Product"
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            model.addAttribute(
                    "isAdmin",
                    isAdmin
            );

            return "products/form";

        } catch (IllegalArgumentException | SecurityException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("productDTO") ProductDTO dto,
            @RequestParam(value = "image", required = false)
            MultipartFile image,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean isAdmin =
                    authentication.getAuthorities()
                            .stream()
                            .anyMatch(
                                    authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_ADMIN")
                            );

            if (isAdmin) {
                productService.updateProductForAdmin(
                        id,
                        dto,
                        image
                );
            } else {
                productService.updateProduct(
                        id,
                        dto,
                        image,
                        authentication.getName()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật Product thành công."
            );

            return "redirect:/products";

        } catch (IllegalArgumentException | SecurityException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "productDTO",
                    dto
            );

            return "redirect:/products/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean isAdmin =
                    authentication.getAuthorities()
                            .stream()
                            .anyMatch(
                                    authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_ADMIN")
                            );

            if (isAdmin) {
                productService.deleteProductForAdmin(id);
            } else {
                productService.deleteProduct(
                        id,
                        authentication.getName()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa Product thành công."
            );

        } catch (IllegalArgumentException | SecurityException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/products";
    }
}
