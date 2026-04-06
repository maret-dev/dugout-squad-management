package com.dugout.controller;

import com.dugout.model.Role;
import com.dugout.repository.TeamRepository;
import com.dugout.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CoachService coachService;
    private final TeamRepository teamRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("coachCount", coachService.getAllCoaches().size());
        model.addAttribute("teamCount", teamRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/coaches")
    public String coaches(Model model) {
        model.addAttribute("coaches", coachService.getAllCoaches());
        return "admin/coaches";
    }

    @GetMapping("/coaches/create")
    public String createCoachForm() {
        return "admin/coach-create";
    }

    @PostMapping("/coaches/create")
    public String createCoach(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String fullName,
                              @RequestParam Role role,
                              RedirectAttributes redirectAttributes) {
        coachService.createCoach(username, password, fullName, role);
        redirectAttributes.addFlashAttribute("successMessage", "Coach account created successfully.");
        return "redirect:/admin/coaches";
    }

    @PostMapping("/coaches/{id}/toggle")
    public String toggleCoach(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        coachService.toggleCoachEnabled(id);
        redirectAttributes.addFlashAttribute("successMessage", "Coach status updated.");
        return "redirect:/admin/coaches";
    }

    @GetMapping("/teams")
    public String allTeams(Model model) {
        model.addAttribute("teams", teamRepository.findAll());
        return "admin/all-teams";
    }
}
