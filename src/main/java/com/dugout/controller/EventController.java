package com.dugout.controller;

import com.dugout.model.EventType;
import com.dugout.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/teams/{teamId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/add")
    public String addEvent(@PathVariable Long teamId,
                           @RequestParam EventType type,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(required = false) String notes,
                           RedirectAttributes redirectAttributes) {
        eventService.addEvent(teamId, type, date, notes);
        redirectAttributes.addFlashAttribute("successMessage", "Event added successfully.");
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/{eid}/delete")
    public String deleteEvent(@PathVariable Long teamId,
                              @PathVariable Long eid,
                              RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(teamId, eid);
        redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        return "redirect:/teams/" + teamId;
    }
}
