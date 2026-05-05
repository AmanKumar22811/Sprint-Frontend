package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.CustomerDto;
import com.classicmodel.frontend.dto.PaymentDto;
import com.classicmodel.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(Model model) {
        List<PaymentDto> payments = apiService.getAllPayments();
        model.addAttribute("payments", payments);
        model.addAttribute("count", payments.size());
        return "payments/list";
    }

    @GetMapping("/{customerNumber}/{checkNumber}")
    public String view(@PathVariable Integer customerNumber,
                       @PathVariable String checkNumber, Model model) {
        try {
            PaymentDto payment = apiService.getPayment(String.valueOf(customerNumber), checkNumber);
            model.addAttribute("payment", payment);
            return "payments/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Payment Not Found");
            model.addAttribute("errorMessage", "Payment for customer #" + customerNumber + " check " + checkNumber + " could not be found.");
            return "error";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<CustomerDto> customers = apiService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "payments/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam Integer customerNumber,
                         @RequestParam String checkNumber,
                         @RequestParam String paymentDate,
                         @RequestParam String amount,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":{");
            sb.append("\"customerNumber\":").append(customerNumber).append(",");
            sb.append("\"checkNumber\":\"").append(escape(checkNumber)).append("\"");
            sb.append("},");
            sb.append("\"customer\":\"/api/customers/").append(customerNumber).append("\",");
            sb.append("\"paymentDate\":\"").append(paymentDate).append("\",");
            sb.append("\"amount\":").append(amount);
            sb.append("}");
            apiService.createPayment(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Payment created successfully.");
            return "redirect:/payments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/payments/new";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
