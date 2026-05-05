package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.CustomerDto;
import com.classicmodel.frontend.dto.EmployeeDto;
import com.classicmodel.frontend.dto.OrderDto;
import com.classicmodel.frontend.dto.PaymentDto;
import com.classicmodel.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        ApiService.PageResult<CustomerDto> result = apiService.getPagedCustomers(page);
        model.addAttribute("customers", result.items);
        model.addAttribute("count", result.totalElements);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalPages", result.totalPages);
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Integer id, Model model) {
        try {
            CustomerDto customer = apiService.getCustomer(id);
            List<OrderDto> orders = apiService.getOrdersByCustomer(id);
            List<PaymentDto> payments = apiService.getPaymentsByCustomer(id);
            model.addAttribute("customer", customer);
            model.addAttribute("orders", orders);
            model.addAttribute("payments", payments);
            return "customers/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Customer Not Found");
            model.addAttribute("errorMessage", "Customer #" + id + " could not be found.");
            return "error";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            CustomerDto customer = apiService.getCustomer(id);
            List<EmployeeDto> employees = apiService.getAllEmployees();
            model.addAttribute("customer", customer);
            model.addAttribute("employees", employees);
            return "customers/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Customer Not Found");
            model.addAttribute("errorMessage", "Customer #" + id + " could not be found.");
            return "error";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id,
                         @RequestParam String customerName,
                         @RequestParam String contactLastName,
                         @RequestParam String contactFirstName,
                         @RequestParam String phone,
                         @RequestParam String addressLine1,
                         @RequestParam(required = false) String addressLine2,
                         @RequestParam String city,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String postalCode,
                         @RequestParam String country,
                         @RequestParam(required = false) String creditLimit,
                         @RequestParam(required = false) String salesRepEmployeeNumber,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"customerName\":\"").append(escape(customerName)).append("\",");
            sb.append("\"contactLastName\":\"").append(escape(contactLastName)).append("\",");
            sb.append("\"contactFirstName\":\"").append(escape(contactFirstName)).append("\",");
            sb.append("\"phone\":\"").append(escape(phone)).append("\",");
            sb.append("\"addressLine1\":\"").append(escape(addressLine1)).append("\",");
            sb.append("\"addressLine2\":\"").append(escape(addressLine2 != null ? addressLine2 : "")).append("\",");
            sb.append("\"city\":\"").append(escape(city)).append("\",");
            sb.append("\"state\":\"").append(escape(state != null ? state : "")).append("\",");
            sb.append("\"postalCode\":\"").append(escape(postalCode != null ? postalCode : "")).append("\",");
            sb.append("\"country\":\"").append(escape(country)).append("\"");
            if (creditLimit != null && !creditLimit.isEmpty()) {
                sb.append(",\"creditLimit\":").append(creditLimit);
            }
            if (salesRepEmployeeNumber != null && !salesRepEmployeeNumber.isEmpty()) {
                sb.append(",\"salesRepEmployee\":\"/api/employees/").append(escape(salesRepEmployeeNumber)).append("\"");
            }
            sb.append("}");
            apiService.updateCustomer(id, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully.");
            return "redirect:/customers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/customers/" + id + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<EmployeeDto> employees = apiService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "customers/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam Integer customerNumber,
                         @RequestParam String customerName,
                         @RequestParam String contactLastName,
                         @RequestParam String contactFirstName,
                         @RequestParam String phone,
                         @RequestParam String addressLine1,
                         @RequestParam(required = false) String addressLine2,
                         @RequestParam String city,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String postalCode,
                         @RequestParam String country,
                         @RequestParam(required = false) String creditLimit,
                         @RequestParam(required = false) String salesRepEmployeeNumber,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"customerNumber\":").append(customerNumber).append(",");
            sb.append("\"customerName\":\"").append(escape(customerName)).append("\",");
            sb.append("\"contactLastName\":\"").append(escape(contactLastName)).append("\",");
            sb.append("\"contactFirstName\":\"").append(escape(contactFirstName)).append("\",");
            sb.append("\"phone\":\"").append(escape(phone)).append("\",");
            sb.append("\"addressLine1\":\"").append(escape(addressLine1)).append("\",");
            sb.append("\"addressLine2\":\"").append(escape(addressLine2 != null ? addressLine2 : "")).append("\",");
            sb.append("\"city\":\"").append(escape(city)).append("\",");
            sb.append("\"state\":\"").append(escape(state != null ? state : "")).append("\",");
            sb.append("\"postalCode\":\"").append(escape(postalCode != null ? postalCode : "")).append("\",");
            sb.append("\"country\":\"").append(escape(country)).append("\"");
            if (creditLimit != null && !creditLimit.isEmpty()) {
                sb.append(",\"creditLimit\":").append(creditLimit);
            }
            if (salesRepEmployeeNumber != null && !salesRepEmployeeNumber.isEmpty()) {
                sb.append(",\"salesRepEmployee\":\"/api/employees/").append(escape(salesRepEmployeeNumber)).append("\"");
            }
            sb.append("}");
            apiService.createCustomer(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully.");
            return "redirect:/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/customers/new";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
