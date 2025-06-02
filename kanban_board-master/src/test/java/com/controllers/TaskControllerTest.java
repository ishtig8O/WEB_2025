package com.controllers;

import com.config.SpringConfig;
import com.dao.TaskDAO;
import com.dao.UserDAO;
import com.models.Task;
import com.models.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = SpringConfig.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskDAO taskDAO;

    @MockBean
    private UserDAO userDAO;

    @Test
    public void deleteTask_ShouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(post("/home/delete")
                        .param("taskId", "3"))
                .andExpect(redirectedUrl("/home"));
        Mockito.verify(taskDAO).removeTask(3L);
    }

    @Test
    public void homePage_ShouldReturnTask_WhenUserIsLoggedIn() throws Exception {
        Long userId = 1L;
        User user = new User(userId, "кошка");
        List<Task> tasks = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        tasks.add(new Task(1L, "Task 1", "1",
                "Open", localDateTime, user, 5L));
        tasks.add(new Task(2L, "Task 2", "2",
                "Open", localDateTime, user, 5L));

        Mockito.when(taskDAO.getTasksByUser(userId)).thenReturn(tasks);
        Mockito.when(userDAO.getAllUsers()).thenReturn(new ArrayList<>());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);

        mockMvc.perform(get("/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/home"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attribute("tasks", tasks));
    }

    @Test
    public void createTask_ShouldCreateTaskAndRedirect() throws Exception {
        Long userId = 1L;
        Long assigneeId = 2L;
        User assignee = new User(assigneeId, "лиса");

        Mockito.when(userDAO.getUserById(assigneeId)).thenReturn(assignee);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);

        Task task = new Task();
        task.setTitle("New task");
        task.setDescription("123");
        task.setAssignee(assignee);
        task.setCreatedBy(userId);

        mockMvc.perform(post("/home/create")
                        .param("title", "New task")
                        .param("description", "123")
                        .param("assignee.id", assigneeId.toString())
                        .session(session))
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("message", "Задача создана!"));

        Mockito.verify(taskDAO).addTask(Mockito.any(Task.class));
    }

    @Test
    public void updateStatus_ShouldUpdateStatusTaskAndRedirect() throws Exception {
        mockMvc.perform(post("/home/status")
                        .param("taskId", "1")
                        .param("status", "Open"))
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("message", "Статус обновлен!"));

        Mockito.verify(taskDAO).changeStatus(1L, "Open");
    }

    @Test
    public void assignTask_ShouldAssignTaskToUserAndRedirect() throws Exception {
        User userBy = new User(1L, "кошка");
        User assignee = new User(2L, "лиса");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("нью таске");
        task.setDescription("123");
        task.setCreatedAt(LocalDateTime.now());
        task.setAssignee(null);
        task.setCreatedBy(userBy.getId());

        Mockito.when(taskDAO.getTaskById(task.getId())).thenReturn(task);
        Mockito.when(userDAO.getUserById(assignee.getId())).thenReturn(assignee);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userBy.getId());

        mockMvc.perform(post("/home/assign")
                        .param("taskId", task.getId().toString())
                        .param("assigneeId", assignee.getId().toString())
                        .session(session))
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("message", "Исполнитель обновлен!"));

        Mockito.verify(taskDAO).updateAssignee(task.getId(), assignee.getId());
    }
}
