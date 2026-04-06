package com.dugout.controller;

import com.dugout.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/teams/{teamId}/attendance")
    public String saveAttendance(@PathVariable Long teamId,
                                 @RequestParam Map<String, String> allParams,
                                 RedirectAttributes redirectAttributes) {
        Map<String, Boolean> attendanceData = new HashMap<>();

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("att_")) {
                String attendanceKey = key.substring(4); // Remove "att_" prefix
                attendanceData.put(attendanceKey, "true".equals(entry.getValue()));
            }
        }

        attendanceService.saveAttendance(teamId, attendanceData);
        redirectAttributes.addFlashAttribute("successMessage", "Attendance saved successfully.");
        return "redirect:/teams/" + teamId;
    }
}
