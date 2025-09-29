package com.todo.controller;

import com.todo.model.Task;
import com.todo.model.User;
import com.todo.service.TaskService;
import com.todo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MainController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskService taskService;

    // Home page
    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    // Login page
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    // Register page
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    // Login processing
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       HttpSession session, 
                       Model model) {
        try {
            Optional<User> user = userService.login(username, password);
            if (user.isPresent()) {
                session.setAttribute("user", user.get());
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    // Register processing
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getUserTasks(user);
        long totalTasks = taskService.getTotalTaskCount(user);
        long completedTasks = taskService.getCompletedTaskCount(user);

        model.addAttribute("tasks", tasks);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        return "dashboard";
    }

    // ========== REST API ENDPOINTS ==========
    
    // Create task - REST API
    @PostMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<?> createTaskApi(@RequestParam String title,
                                         @RequestParam String description,
                                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("Not authenticated"));
        }

        try {
            Task task = new Task(title, description, user);
            Task savedTask = taskService.createTask(task);
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error creating task: " + e.getMessage()));
        }
    }

    // Update task status - REST API
    @PostMapping("/api/tasks/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleTaskApi(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("Not authenticated"));
        }

        try {
            Optional<Task> task = taskService.getTaskById(id);
            if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
                task.get().setCompleted(!task.get().isCompleted());
                Task updatedTask = taskService.updateTask(task.get());
                return ResponseEntity.ok(updatedTask);
            }
            return ResponseEntity.status(404).body(createErrorResponse("Task not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error toggling task: " + e.getMessage()));
        }
    }

    // Delete task - REST API
    @PostMapping("/api/tasks/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteTaskApi(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("Not authenticated"));
        }

        try {
            Optional<Task> task = taskService.getTaskById(id);
            if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
                taskService.deleteTask(id);
                return ResponseEntity.ok(createSuccessResponse("Task deleted successfully"));
            }
            return ResponseEntity.status(404).body(createErrorResponse("Task not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error deleting task: " + e.getMessage()));
        }
    }

    // Update task - REST API
    @PostMapping("/api/tasks/{id}/update")
    @ResponseBody
    public ResponseEntity<?> updateTaskApi(@PathVariable Long id,
                                         @RequestParam String title,
                                         @RequestParam String description,
                                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("Not authenticated"));
        }

        try {
            Optional<Task> task = taskService.getTaskById(id);
            if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
                task.get().setTitle(title);
                task.get().setDescription(description);
                Task updatedTask = taskService.updateTask(task.get());
                return ResponseEntity.ok(updatedTask);
            }
            return ResponseEntity.status(404).body(createErrorResponse("Task not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error updating task: " + e.getMessage()));
        }
    }

    // Get tasks - REST API
    @GetMapping("/api/tasks")
    @ResponseBody
    public ResponseEntity<?> getTasksApi(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("Not authenticated"));
        }

        try {
            List<Task> tasks = taskService.getUserTasks(user);
            long totalTasks = taskService.getTotalTaskCount(user);
            long completedTasks = taskService.getCompletedTaskCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tasks", tasks);
            response.put("totalTasks", totalTasks);
            response.put("completedTasks", completedTasks);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Error fetching tasks: " + e.getMessage()));
        }
    }

    // ========== EXISTING THYMELEAF ENDPOINTS (keep these) ==========
    
    // Create task - Thymeleaf
    @PostMapping("/tasks")
    public String createTask(@RequestParam String title,
                           @RequestParam String description,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Task task = new Task(title, description, user);
        taskService.createTask(task);
        return "redirect:/dashboard";
    }

    // Update task status - Thymeleaf
    @PostMapping("/tasks/{id}/toggle")
    public String toggleTask(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
            task.get().setCompleted(!task.get().isCompleted());
            taskService.updateTask(task.get());
        }
        return "redirect:/dashboard";
    }

    // Delete task - Thymeleaf
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
            taskService.deleteTask(id);
        }
        return "redirect:/dashboard";
    }

    // Edit task form - Thymeleaf
    @GetMapping("/tasks/{id}/edit")
    public String editTaskForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
            model.addAttribute("task", task.get());
            return "edit-task";
        }
        return "redirect:/dashboard";
    }

    // Update task - Thymeleaf
    @PostMapping("/tasks/{id}/update")
    public String updateTask(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam String description,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent() && task.get().getUser().getId().equals(user.getId())) {
            task.get().setTitle(title);
            task.get().setDescription(description);
            taskService.updateTask(task.get());
        }
        return "redirect:/dashboard";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ========== HELPER METHODS ==========
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
    
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}