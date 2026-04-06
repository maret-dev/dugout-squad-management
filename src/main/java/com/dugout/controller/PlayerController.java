package com.dugout.controller;

import com.dugout.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teams/{teamId}/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/add")
    public String addPlayer(@PathVariable Long teamId,
                            @RequestParam String firstName,
                            @RequestParam String lastName,
                            RedirectAttributes redirectAttributes) {
        playerService.addPlayer(teamId, firstName, lastName);
        redirectAttributes.addFlashAttribute("successMessage", "Player added successfully.");
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/{pid}/remove")
    public String removePlayer(@PathVariable Long teamId,
                               @PathVariable Long pid,
                               RedirectAttributes redirectAttributes) {
        playerService.removePlayer(teamId, pid);
        redirectAttributes.addFlashAttribute("successMessage", "Player removed successfully.");
        return "redirect:/teams/" + teamId;
    }
}
