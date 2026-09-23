package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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

        Page<UserDTO> userPage =
                userService.getAllUsers(
                        keyword,
                        pageable
                );

        model.addAttribute(
                "users",
                userPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                userPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                userPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                userPage.getTotalElements()
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
                "totalUsers",
                userService.countUsers()
        );

        return "users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("userDTO")) {
            UserDTO dto = new UserDTO();

            dto.setRole("ROLE_USER");
            dto.setEnabled(true);

            model.addAttribute(
                    "userDTO",
                    dto
            );
        }

        model.addAttribute(
                "formTitle",
                "Thêm User"
        );

        return "users/form";
    }

    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute("userDTO") UserDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.createUser(dto);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Tạo User thành công. Password mặc định là 123456."
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "userDTO",
                    dto
            );

            return "redirect:/users/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserDTO dto = userService.getUserById(id);

            model.addAttribute(
                    "userDTO",
                    dto
            );

            model.addAttribute(
                    "formTitle",
                    "Chỉnh sửa User"
            );

            return "users/form";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/users";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("userDTO") UserDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.updateUser(
                    id,
                    dto
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật User thành công."
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "userDTO",
                    dto
            );

            return "redirect:/users/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa User thành công."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/users";
    }
}
