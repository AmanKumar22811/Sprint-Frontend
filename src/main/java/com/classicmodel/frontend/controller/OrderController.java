package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.CustomerDto;
import com.classicmodel.frontend.dto.OrderDetailDto;
import com.classicmodel.frontend.dto.OrderDto;
import com.classicmodel.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        ApiService.PageResult<OrderDto> result = apiService.getPagedOrders(page);
        model.addAttribute("orders", result.items);
        model.addAttribute("count", result.totalElements);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalPages", result.totalPages);
        return "orders/list";
    }

    @GetMapping("/{orderNumber}")
    public String view(@PathVariable Integer orderNumber, Model model) {
        try {
            OrderDto order = apiService.getOrder(orderNumber);
            List<OrderDetailDto> details = apiService.getOrderDetails(orderNumber);
            model.addAttribute("order", order);
            model.addAttribute("details", details);
            return "orders/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Order Not Found");
            model.addAttribute("errorMessage", "Order #" + orderNumber + " could not be found.");
            return "error";
        }
    }

    @GetMapping("/{orderNumber}/edit")
    public String editForm(@PathVariable Integer orderNumber, Model model) {
        try {
            OrderDto order = apiService.getOrder(orderNumber);
            model.addAttribute("order", order);
            return "orders/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Order Not Found");
            model.addAttribute("errorMessage", "Order #" + orderNumber + " could not be found.");
            return "error";
        }
    }

    @PostMapping("/{orderNumber}/edit")
    public String update(@PathVariable Integer orderNumber,
                         @RequestParam String orderDate,
                         @RequestParam String requiredDate,
                         @RequestParam(required = false) String shippedDate,
                         @RequestParam String status,
                         @RequestParam(required = false) String comments,
                         RedirectAttributes redirectAttributes) {

        String validationError = validateDates(orderDate, requiredDate, shippedDate);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return "redirect:/orders/" + orderNumber + "/edit";
        }

        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"orderDate\":\"").append(orderDate).append("\",");
            sb.append("\"requiredDate\":\"").append(requiredDate).append("\",");
            if (shippedDate != null && !shippedDate.isEmpty()) {
                sb.append("\"shippedDate\":\"").append(shippedDate).append("\",");
            }
            sb.append("\"status\":\"").append(escape(status)).append("\"");
            if (comments != null && !comments.isEmpty()) {
                sb.append(",\"comments\":\"").append(escape(comments)).append("\"");
            }
            sb.append("}");
            apiService.updateOrder(orderNumber, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully.");
            return "redirect:/orders/" + orderNumber;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed. Please check your input and try again.");
            return "redirect:/orders/" + orderNumber + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<CustomerDto> customers = apiService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "orders/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam Integer orderNumber,
                         @RequestParam String orderDate,
                         @RequestParam String requiredDate,
                         @RequestParam(required = false) String shippedDate,
                         @RequestParam String status,
                         @RequestParam(required = false) String comments,
                         @RequestParam Integer customerNumber,
                         RedirectAttributes redirectAttributes) {

        String validationError = validateDates(orderDate, requiredDate, shippedDate);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return "redirect:/orders/new";
        }

        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"orderNumber\":").append(orderNumber).append(",");
            sb.append("\"orderDate\":\"").append(orderDate).append("\",");
            sb.append("\"requiredDate\":\"").append(requiredDate).append("\",");
            if (shippedDate != null && !shippedDate.isEmpty()) {
                sb.append("\"shippedDate\":\"").append(shippedDate).append("\",");
            }
            sb.append("\"status\":\"").append(escape(status)).append("\",");
            if (comments != null && !comments.isEmpty()) {
                sb.append("\"comments\":\"").append(escape(comments)).append("\",");
            }
            sb.append("\"customer\":\"/api/customers/").append(customerNumber).append("\"");
            sb.append("}");
            apiService.createOrder(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully.");
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed. Please check your input and try again.");
            return "redirect:/orders/new";
        }
    }

    /**
     * Validates date logic. Returns a human-readable error message, or null if valid.
     */
    private String validateDates(String orderDate, String requiredDate, String shippedDate) {
        try {
            LocalDate od = LocalDate.parse(orderDate);

            if (requiredDate != null && !requiredDate.isEmpty()) {
                LocalDate rd = LocalDate.parse(requiredDate);
                if (rd.isBefore(od)) {
                    return "Required date (" + requiredDate + ") cannot be before the order date (" + orderDate + ").";
                }
            }

            if (shippedDate != null && !shippedDate.isEmpty()) {
                LocalDate sd = LocalDate.parse(shippedDate);
                if (sd.isBefore(od)) {
                    return "Shipped date (" + shippedDate + ") cannot be before the order date (" + orderDate + ").";
                }
            }
        } catch (Exception e) {
            return "Invalid date format. Please use the date picker to select dates.";
        }
        return null;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
