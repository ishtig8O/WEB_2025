package com.controllers;

import com.dao.TaskDAO;
import com.dao.UserDAO;
import com.models.Task;
import com.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/home")
public class TaskController {

    private final TaskDAO taskDAO;
    private final UserDAO userDAO;

    @Autowired
    public TaskController(TaskDAO taskDAO, UserDAO userDAO) {
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
    }

    // главная страница
    @GetMapping
    public String mainPage(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("tasks", taskDAO.getTasksByUser(userId));
        } else {
            model.addAttribute("error", "Нужно войти в систему");
            return "auth/login";
        }

        model.addAttribute("users", userDAO.getAllUsers());
        return "tasks/home";
    }

    // создание новой задачи
    @PostMapping("/create")
    public String addTask(
            @ModelAttribute("task") Task task,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("error", "Нужно войти в систему");
            return "redirect:/auth/login";
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("OPEN");
        task.setCreatedBy(currentUserId);

        User assignee = userDAO.getUserById(task.getAssignee().getId());
        if (assignee != null) {
            task.setAssignee(assignee);
        } else {
            redirectAttributes.addFlashAttribute("error", "Исполнитель не найден");
            return "redirect:/home";
        }

        taskDAO.addTask(task);
        redirectAttributes.addFlashAttribute("message", "Задача создана!");
        return "redirect:/home";
    }

    // обновление статуса задачи
    @PostMapping("/status")
    public String changeStatus(
            @RequestParam("taskId") Long taskId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {

        taskDAO.changeStatus(taskId, status);
        redirectAttributes.addFlashAttribute("message", "Статус обновлен!");
        return "redirect:/home";
    }

    // смена исполнителя задачи
    @PostMapping("/assign")
    public String reassignTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("assigneeId") Long assigneeId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = (Long) session.getAttribute("userId");

        Task task = taskDAO.getTaskById(taskId);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Задача не найдена");
            return "redirect:/home";
        }

        if (task.getAssignee() != null && task.getAssignee().getId().equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "Нет доступа для изменения");
            return "redirect:/home";
        }

        User assignee = userDAO.getUserById(assigneeId);
        if (assignee != null) {
            taskDAO.updateAssignee(taskId, assigneeId);
            redirectAttributes.addFlashAttribute("message", "Исполнитель обновлен!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Исполнитель не найден");
        }

        return "redirect:/home";
    }

    // выход
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // удаление задачи
    @PostMapping("/delete")
    public String removeTask(@RequestParam("taskId") Long taskId, RedirectAttributes redirectAttributes) {
        taskDAO.removeTask(taskId);
        redirectAttributes.addFlashAttribute("message", "Задача удалена!");
        return "redirect:/home";
    }
}
