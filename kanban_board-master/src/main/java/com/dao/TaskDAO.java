package com.dao;

import com.models.Task;
import com.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class TaskDAO {

    private final JdbcTemplate jdbcTemplate;
    private final UserDAO userDAO;

    @Autowired
    public TaskDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
    }

    // найти задачу по id
    public Task getTaskById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long assignedTo = rs.getLong("assigned_to");
            if (!rs.wasNull()) {
                User assignee = userDAO.getUserById(assignedTo);
                task.setAssignee(assignee);
            }

            task.setCreatedBy(rs.getLong("created_by"));
            return task;
        });
    }

    // добавить новую задачу
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, created_by, assigned_to, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getCreatedBy(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                "OPEN",
                java.sql.Timestamp.valueOf(task.getCreatedAt())
        );
    }

    // обновить статус задачи
    public boolean changeStatus(Long taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, status, taskId);
        return updated > 0;
    }

    // назначить исполнителя
    public boolean updateAssignee(Long taskId, Long assigneeId) {
        String sql = "UPDATE tasks SET assigned_to = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, assigneeId, taskId);
        return updated > 0;
    }

    // удалить задачу
    public void removeTask(Long taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, taskId);
    }

    // найти все задачи пользователя
    public List<Task> getTasksByUser(Long userId) {
        String sql = "SELECT * FROM tasks WHERE created_by = ? OR assigned_to = ?";
        return jdbcTemplate.query(sql, new Object[]{userId, userId}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setStatus(rs.getString("status"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Long assignedTo = rs.getLong("assigned_to");
            if (!rs.wasNull()) {
                User assignee = userDAO.getUserById(assignedTo);
                task.setAssignee(assignee);
            }

            task.setCreatedBy(rs.getLong("created_by"));
            return task;
        });
    }
}
