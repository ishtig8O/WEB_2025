package com.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    // перенаправление на страницу авторизации
    @GetMapping("/")
    public String goToLogin() {
        return "redirect:/auth/login";
    }
}
