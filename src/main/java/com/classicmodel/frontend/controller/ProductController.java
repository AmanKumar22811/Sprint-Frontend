package com.classicmodel.frontend.controller;

import com.classicmodel.frontend.dto.ProductDto;
import com.classicmodel.frontend.dto.ProductLineDto;
import com.classicmodel.frontend.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(Model model) {
        List<ProductDto> products = apiService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("count", products.size());
        return "products/list";
    }

    @GetMapping("/{code}")
    public String view(@PathVariable String code, Model model) {
        try {
            ProductDto product = apiService.getProduct(code);
            model.addAttribute("product", product);
            return "products/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Product Not Found");
            model.addAttribute("errorMessage", "Product '" + code + "' could not be found.");
            return "error";
        }
    }

    @GetMapping("/{code}/edit")
    public String editForm(@PathVariable String code, Model model) {
        try {
            ProductDto product = apiService.getProduct(code);
            List<ProductLineDto> productLines = apiService.getAllProductLines();
            model.addAttribute("product", product);
            model.addAttribute("productLines", productLines);
            return "products/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Product Not Found");
            model.addAttribute("errorMessage", "Product '" + code + "' could not be found.");
            return "error";
        }
    }

    @PostMapping("/{code}/edit")
    public String update(@PathVariable String code,
                         @RequestParam String productName,
                         @RequestParam String productLine,
                         @RequestParam String productScale,
                         @RequestParam String productVendor,
                         @RequestParam String productDescription,
                         @RequestParam Short quantityInStock,
                         @RequestParam String buyPrice,
                         @RequestParam String msrp,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"productName\":\"").append(escape(productName)).append("\",");
            sb.append("\"productLine\":\"/api/productlines/").append(escape(productLine)).append("\",");
            sb.append("\"productScale\":\"").append(escape(productScale)).append("\",");
            sb.append("\"productVendor\":\"").append(escape(productVendor)).append("\",");
            sb.append("\"productDescription\":\"").append(escape(productDescription)).append("\",");
            sb.append("\"quantityInStock\":").append(quantityInStock).append(",");
            sb.append("\"buyPrice\":").append(buyPrice).append(",");
            sb.append("\"MSRP\":").append(msrp);
            sb.append("}");
            apiService.updateProduct(code, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
            return "redirect:/products/" + code;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/products/" + code + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<ProductLineDto> productLines = apiService.getAllProductLines();
        model.addAttribute("productLines", productLines);
        return "products/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam String productCode,
                         @RequestParam String productName,
                         @RequestParam String productLine,
                         @RequestParam String productScale,
                         @RequestParam String productVendor,
                         @RequestParam String productDescription,
                         @RequestParam Short quantityInStock,
                         @RequestParam String buyPrice,
                         @RequestParam String msrp,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"productCode\":\"").append(escape(productCode)).append("\",");
            sb.append("\"productName\":\"").append(escape(productName)).append("\",");
            sb.append("\"productLine\":\"/api/productlines/").append(escape(productLine)).append("\",");
            sb.append("\"productScale\":\"").append(escape(productScale)).append("\",");
            sb.append("\"productVendor\":\"").append(escape(productVendor)).append("\",");
            sb.append("\"productDescription\":\"").append(escape(productDescription)).append("\",");
            sb.append("\"quantityInStock\":").append(quantityInStock).append(",");
            sb.append("\"buyPrice\":").append(buyPrice).append(",");
            sb.append("\"MSRP\":").append(msrp);
            sb.append("}");
            apiService.createProduct(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/products/new";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
