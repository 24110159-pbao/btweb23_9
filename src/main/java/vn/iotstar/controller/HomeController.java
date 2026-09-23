package vn.iotstar.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.security.CustomUserDetails;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(
            Authentication authentication,
            Model model
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "home";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {

            model.addAttribute(
                    "currentUser",
                    userDetails
            );

            model.addAttribute(
                    "username",
                    userDetails.getUsername()
            );

            model.addAttribute(
                    "email",
                    userDetails.getEmail()
            );

            model.addAttribute(
                    "fullName",
                    userDetails.getFullName()
            );

            model.addAttribute(
                    "authorities",
                    userDetails.getAuthorities()
            );
        }

        return "home";
    }
}
