package com.example.lab5.controller;

import com.example.lab5.model.Category;
import com.example.lab5.model.Task;
import com.example.lab5.model.Status;
import com.example.lab5.model.User;
import com.example.lab5.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StatusService taskStatusService;

    @Autowired
    private PriorityService taskPriorityService;

    @Autowired
    private UserService userService;
    //обрабатывает гет запрос на /tasks, отображает список задач для текущего пользователя с возможностью фильтрации по статусу и категории
    @GetMapping
    public String listTasks(Authentication authentication, Model model,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(required = false) Long statusId) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Task> tasks;

        if (categoryId != null || statusId != null) {
            tasks = taskService.filterTasksByStatusOrCategory(user, statusId, categoryId);
        } else {
            tasks = taskService.getAllTasksForUser(user);
        }

        List<Category> categories = categoryService.getAllCategories();
        List<Status> statuses = taskStatusService.getAllStatuses();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categories);
        model.addAttribute("statuses", statuses);

        return "task/list";
    }
    //обрабатывает гет запрос на /tasks/new, отображает форму для создания новой задачи
    @GetMapping("/new")
    public String showCreateTaskForm(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", taskStatusService.getAllStatuses());
        model.addAttribute("priorities", taskPriorityService.getAllPriorities());

        return "task/edit";
    }
    //обрабатывает пост запросы на /tasks, создает новую задачу для текущего пользователя
    @PostMapping
    public String createTask(@Valid @ModelAttribute("task") Task task,
                             BindingResult result,
                             Authentication authentication,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", taskStatusService.getAllStatuses());
            model.addAttribute("priorities", taskPriorityService.getAllPriorities());
            return "task/edit";
        }
        User user = userService.getUserByUsername(authentication.getName());
        if (task.getDueDate().isBefore(LocalDate.now())) {
            result.rejectValue("dueDate", "error.task", "Due date must be in the future");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", taskStatusService.getAllStatuses());
            model.addAttribute("priorities", taskPriorityService.getAllPriorities());
            return "task/edit";
        }
        taskService.createTask(task, user);

        return "redirect:/tasks";
    }
    //обрабатывает гет запросы на /tasks/edit/{id}, отображает форму для редактирования уже имеющихся задач
    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        Task task = taskService.getTaskByIdForUser(id, user).orElse(null);

        if (task == null) {
            return "redirect:/tasks";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("task", task);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", taskStatusService.getAllStatuses());
        model.addAttribute("priorities", taskPriorityService.getAllPriorities());

        return "task/edit";
    }
    //обрабатывает пост запрос на /tasks/edit/{id}, обновляет существующую задачу для текущего пользователя и проверяет корректность изменений
    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @Valid @ModelAttribute("task") Task updatedTask,
                           BindingResult result,
                           Authentication authentication,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", taskStatusService.getAllStatuses());
            model.addAttribute("priorities", taskPriorityService.getAllPriorities());
            return "task/edit";
        }

        User user = userService.getUserByUsername(authentication.getName());
        Task existingTask = taskService.getTaskByIdForUser(id, user).orElse(null);

        if (existingTask == null || existingTask.getUser() == null || !existingTask.getUser().equals(user)) {
            return "redirect:/tasks";
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setCategory(updatedTask.getCategory());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());

        taskService.updateTask(id, existingTask, user);

        return "redirect:/tasks";
    }
    //обрабатывает пост запрос на /tasks/complete/{id}, помечает задачу как завершенную
    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable Long id, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Task task = taskService.getTaskByIdForUser(id, user).orElse(null);

        if (task != null && task.getUser() != null && task.getUser().equals(user)) {
            Optional<Status> completedStatusOptional = taskStatusService.getStatusByName("Completed");
            if (completedStatusOptional.isPresent()) {
                Status completedStatus = completedStatusOptional.get();
                task.setStatus(completedStatus);
                taskService.updateTask(id, task, user);
            }
        }

        return "redirect:/tasks";
    }
    //обрабатывает гет запрос на /tasks/delete/{id}, удаляет задачу для текущего пользователя
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Task task = taskService.getTaskByIdForUser(id, user).orElse(null);

        if (task != null && task.getUser() != null && task.getUser().equals(user)) {
            taskService.deleteTask(id, user);
        }

        return "redirect:/tasks";
    }
    //обрабатывает гет запрос на /tasks/{id},отображает детали задачи для текущего пользователя
    @GetMapping("/{id}")
    public String viewTaskDetails(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        Task task = taskService.getTaskByIdForUser(id, user).orElse(null);

        if (task == null || task.getUser() == null || !task.getUser().equals(user)) {
            return "redirect:/tasks";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("task", task);
        return "task/details";
    }
}
