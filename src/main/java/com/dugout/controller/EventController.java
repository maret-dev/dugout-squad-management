package com.dugout.controller;

import com.dugout.model.EventType;
import com.dugout.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/add-bulk")
    public String addBulkEvents(@PathVariable Long teamId,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam Map<String, String> allParams,
                                RedirectAttributes redirectAttributes) {
        Map<DayOfWeek, EventType> dayMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("day_") && !entry.getValue().isEmpty()) {
                String dayName = entry.getKey().substring(4);
                DayOfWeek dow = DayOfWeek.valueOf(dayName);
                EventType type = EventType.valueOf(entry.getValue());
                dayMapping.put(dow, type);
            }
        }

        if (dayMapping.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Select at least one day.");
            return "redirect:/teams/" + teamId;
        }

        int count = eventService.addBulkEvents(teamId, startDate, endDate, dayMapping);
        redirectAttributes.addFlashAttribute("successMessage", count + " events created successfully.");
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/delete-bulk")
    public String deleteBulkEvents(@PathVariable Long teamId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   RedirectAttributes redirectAttributes) {
        int count = eventService.deleteEventsByDateRange(teamId, startDate, endDate);
        redirectAttributes.addFlashAttribute("successMessage", count + " events deleted successfully.");
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
