package com.controllers;

import com.dao.UserDAO;
import com.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserDAO userDAO;

    @Autowired
    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // показать страницу входа
    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("title", "Авторизация");
        return "auth/login";
    }

    // обработать вход
    @PostMapping("/login")
    public String handleLogin(
            @ModelAttribute("user") User user,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User existingUser = userDAO.getUserByUsername(user.getUsername());

        if (existingUser != null && userDAO.checkUser(user.getUsername(), user.getPassword())) {
            // если логин и пароль правильные — сохраняем в сессию
            session.setAttribute("userId", existingUser.getId());
            return "redirect:/home";
        } else {
            // если ошибка — кидаем флеш сообщение
            redirectAttributes.addFlashAttribute("error", "Неверный логин или пароль");
            return "redirect:/auth/login";
        }
    }

    // показать страницу регистрации
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // обработать регистрацию
    @PostMapping("/register")
    public String handleRegister(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {

        if (userDAO.userExists(user.getUsername())) {
            // если пользователь есть — ошибка
            redirectAttributes.addFlashAttribute("error", "Пользователь с таким логином уже есть");
            return "redirect:/auth/register";
        }

        // сохраняем нового пользователя
        userDAO.addUser(user);
        redirectAttributes.addFlashAttribute("message", "Регистрация прошла успешно! Теперь войдите.");
        return "redirect:/auth/login";
    }
}
