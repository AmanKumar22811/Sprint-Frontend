package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.CustomerDto;
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
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        ApiService.PageResult<EmployeeDto> result = apiService.getPagedEmployees(page);
        model.addAttribute("employees", result.items);
        model.addAttribute("count", result.totalElements);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalPages", result.totalPages);
        return "employees/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Integer id, Model model) {
        try {
            EmployeeDto employee = apiService.getEmployee(id);

            // Direct search — avoids fetching all customers and losing the salesRepEmployee link
            List<CustomerDto> myCustomers = apiService.getCustomersByEmployee(id);

            // Filter all employees by manager id — list endpoint embeds manager data
            List<EmployeeDto> reportees = apiService.getAllEmployees().stream()
                .filter(e -> e.getManager() != null && id.equals(e.getManager().getEmployeeNumber()))
                .collect(java.util.stream.Collectors.toList());

            model.addAttribute("employee", employee);
            model.addAttribute("myCustomers", myCustomers);
            model.addAttribute("reportees", reportees);
            return "employees/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Employee Not Found");
            model.addAttribute("errorMessage", "Employee #" + id + " could not be found.");
            return "error";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<OfficeDto> offices = apiService.getAllOffices();
        List<EmployeeDto> allEmployees = apiService.getAllEmployees();
        model.addAttribute("offices", offices);
        model.addAttribute("allEmployees", allEmployees);
        return "employees/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam Integer employeeNumber,
                         @RequestParam String lastName,
                         @RequestParam String firstName,
                         @RequestParam String extension,
                         @RequestParam String email,
                         @RequestParam String jobTitle,
                         @RequestParam(required = false) String officeCode,
                         @RequestParam(required = false) String managerNumber,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"employeeNumber\":").append(employeeNumber).append(",");
            sb.append("\"lastName\":\"").append(escape(lastName)).append("\",");
            sb.append("\"firstName\":\"").append(escape(firstName)).append("\",");
            sb.append("\"extension\":\"").append(escape(extension)).append("\",");
            sb.append("\"email\":\"").append(escape(email)).append("\",");
            sb.append("\"jobTitle\":\"").append(escape(jobTitle)).append("\"");
            if (officeCode != null && !officeCode.isEmpty()) {
                sb.append(",\"office\":\"/api/offices/").append(escape(officeCode)).append("\"");
            }
            if (managerNumber != null && !managerNumber.isEmpty()) {
                sb.append(",\"manager\":\"/api/employees/").append(escape(managerNumber)).append("\"");
            }
            sb.append("}");
            apiService.createEmployee(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Employee created successfully.");
            return "redirect:/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/employees/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            EmployeeDto employee = apiService.getEmployee(id);
            List<OfficeDto> offices = apiService.getAllOffices();
            List<EmployeeDto> allEmployees = apiService.getAllEmployees();
            model.addAttribute("employee", employee);
            model.addAttribute("offices", offices);
            model.addAttribute("allEmployees", allEmployees);
            return "employees/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Employee Not Found");
            model.addAttribute("errorMessage", "Employee #" + id + " could not be found.");
            return "error";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id,
                         @RequestParam String lastName,
                         @RequestParam String firstName,
                         @RequestParam String extension,
                         @RequestParam String email,
                         @RequestParam String jobTitle,
                         @RequestParam(required = false) String officeCode,
                         @RequestParam(required = false) String managerNumber,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"lastName\":\"").append(escape(lastName)).append("\",");
            sb.append("\"firstName\":\"").append(escape(firstName)).append("\",");
            sb.append("\"extension\":\"").append(escape(extension)).append("\",");
            sb.append("\"email\":\"").append(escape(email)).append("\",");
            sb.append("\"jobTitle\":\"").append(escape(jobTitle)).append("\"");
            if (officeCode != null && !officeCode.isEmpty()) {
                sb.append(",\"office\":\"/api/offices/").append(escape(officeCode)).append("\"");
            }
            if (managerNumber != null && !managerNumber.isEmpty()) {
                sb.append(",\"manager\":\"/api/employees/").append(escape(managerNumber)).append("\"");
            }
            sb.append("}");
            apiService.updateEmployee(id, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully.");
            return "redirect:/employees/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/employees/" + id + "/edit";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
