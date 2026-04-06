package com.dugout.controller;

import com.dugout.model.Team;
import com.dugout.service.PlayerService;
import com.dugout.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final PlayerService playerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teams", teamService.getTeamsForCurrentCoach());
        return "teams/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "teams/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam String season,
                         RedirectAttributes redirectAttributes) {
        teamService.createTeam(name, season);
        redirectAttributes.addFlashAttribute("successMessage", "Team created successfully.");
        return "redirect:/teams";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Team team = teamService.getTeamForCurrentCoach(id);
        model.addAttribute("team", team);
        model.addAttribute("players", playerService.getActivePlayers(id));
        return "teams/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Team team = teamService.getTeamForCurrentCoach(id);
        model.addAttribute("team", team);
        return "teams/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam String season,
                       RedirectAttributes redirectAttributes) {
        teamService.updateTeam(id, name, season);
        redirectAttributes.addFlashAttribute("successMessage", "Team updated successfully.");
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        teamService.deleteTeam(id);
        redirectAttributes.addFlashAttribute("successMessage", "Team deleted successfully.");
        return "redirect:/teams";
    }
}
