package com.dugout.controller;

import com.dugout.model.Coach;
import com.dugout.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CoachService coachService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Coach coach = coachService.getCurrentAuthenticatedCoach();
        model.addAttribute("coachName", coach.getFullName());
        return "dashboard/index";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
