package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.EmployeeDto;
import com.classicmodel.frontend.dto.OfficeDto;
import com.classicmodel.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/offices")
public class OfficeController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        ApiService.PageResult<OfficeDto> result = apiService.getPagedOffices(page);
        model.addAttribute("offices", result.items);
        model.addAttribute("count", result.totalElements);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalPages", result.totalPages);
        return "offices/list";
    }

    @GetMapping("/{code}")
    public String view(@PathVariable String code, Model model) {
        try {
            OfficeDto office = apiService.getOffice(code);
            List<EmployeeDto> employees = apiService.getEmployeesByOffice(code);
            model.addAttribute("office", office);
            model.addAttribute("employees", employees);
            return "offices/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Office Not Found");
            model.addAttribute("errorMessage", "Office '" + code + "' could not be found.");
            return "error";
        }
    }

    @GetMapping("/{code}/edit")
    public String editForm(@PathVariable String code, Model model) {
        try {
            OfficeDto office = apiService.getOffice(code);
            model.addAttribute("office", office);
            return "offices/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Office Not Found");
            model.addAttribute("errorMessage", "Office '" + code + "' could not be found.");
            return "error";
        }
    }

    @PostMapping("/{code}/edit")
    public String update(@PathVariable String code,
                         @RequestParam String city,
                         @RequestParam String phone,
                         @RequestParam String addressLine1,
                         @RequestParam(required = false) String addressLine2,
                         @RequestParam(required = false) String state,
                         @RequestParam String country,
                         @RequestParam String postalCode,
                         @RequestParam String territory,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"city\":\"").append(escape(city)).append("\",");
            sb.append("\"phone\":\"").append(escape(phone)).append("\",");
            sb.append("\"addressLine1\":\"").append(escape(addressLine1)).append("\",");
            sb.append("\"addressLine2\":\"").append(escape(addressLine2 != null ? addressLine2 : "")).append("\",");
            sb.append("\"state\":\"").append(escape(state != null ? state : "")).append("\",");
            sb.append("\"country\":\"").append(escape(country)).append("\",");
            sb.append("\"postalCode\":\"").append(escape(postalCode)).append("\",");
            sb.append("\"territory\":\"").append(escape(territory)).append("\"");
            sb.append("}");
            apiService.updateOffice(code, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Office updated successfully.");
            return "redirect:/offices/" + code;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/offices/" + code + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return "offices/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam String officeCode,
                         @RequestParam String city,
                         @RequestParam String phone,
                         @RequestParam String addressLine1,
                         @RequestParam(required = false) String addressLine2,
                         @RequestParam(required = false) String state,
                         @RequestParam String country,
                         @RequestParam String postalCode,
                         @RequestParam String territory,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"officeCode\":\"").append(escape(officeCode)).append("\",");
            sb.append("\"city\":\"").append(escape(city)).append("\",");
            sb.append("\"phone\":\"").append(escape(phone)).append("\",");
            sb.append("\"addressLine1\":\"").append(escape(addressLine1)).append("\",");
            sb.append("\"addressLine2\":\"").append(escape(addressLine2 != null ? addressLine2 : "")).append("\",");
            sb.append("\"state\":\"").append(escape(state != null ? state : "")).append("\",");
            sb.append("\"country\":\"").append(escape(country)).append("\",");
            sb.append("\"postalCode\":\"").append(escape(postalCode)).append("\",");
            sb.append("\"territory\":\"").append(escape(territory)).append("\"");
            sb.append("}");
            apiService.createOffice(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Office created successfully.");
            return "redirect:/offices";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/offices/new";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
