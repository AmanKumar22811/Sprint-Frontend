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
@RequestMapping("/productlines")
public class ProductLineController {

    @Autowired private ApiService apiService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        ApiService.PageResult<ProductLineDto> result = apiService.getPagedProductLines(page);
        model.addAttribute("productLines", result.items);
        model.addAttribute("count", result.totalElements);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalPages", result.totalPages);
        return "productlines/list";
    }

    @GetMapping("/{name}")
    public String view(@PathVariable String name, Model model) {
        try {
            ProductLineDto productLine = apiService.getProductLine(name);
            List<ProductDto> products = apiService.getProductsByProductLine(name);
            model.addAttribute("productLine", productLine);
            model.addAttribute("products", products);
            return "productlines/view";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Product Line Not Found");
            model.addAttribute("errorMessage", "Product line '" + name + "' could not be found.");
            return "error";
        }
    }

    @GetMapping("/{name}/edit")
    public String editForm(@PathVariable String name, Model model) {
        try {
            ProductLineDto productLine = apiService.getProductLine(name);
            model.addAttribute("productLine", productLine);
            return "productlines/edit";
        } catch (Exception e) {
            model.addAttribute("errorTitle", "Product Line Not Found");
            model.addAttribute("errorMessage", "Product line '" + name + "' could not be found.");
            return "error";
        }
    }

    @PostMapping("/{name}/edit")
    public String update(@PathVariable String name,
                         @RequestParam(required = false) String textDescription,
                         @RequestParam(required = false) String htmlDescription,
                         @RequestParam(required = false) String imageUrl,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"textDescription\":\"").append(escape(textDescription != null ? textDescription : "")).append("\"");
            if (htmlDescription != null && !htmlDescription.isEmpty()) {
                sb.append(",\"htmlDescription\":\"").append(escape(htmlDescription)).append("\"");
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                sb.append(",\"image\":\"").append(escape(imageUrl)).append("\"");
            }
            sb.append("}");
            apiService.updateProductLine(name, sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Product line updated successfully.");
            return "redirect:/productlines/" + name;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/productlines/" + name + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return "productlines/create";
    }

    @PostMapping("/new")
    public String create(@RequestParam String productLine,
                         @RequestParam(required = false) String textDescription,
                         @RequestParam(required = false) String htmlDescription,
                         @RequestParam(required = false) String imageUrl,
                         RedirectAttributes redirectAttributes) {
        try {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"productLine\":\"").append(escape(productLine)).append("\"");
            if (textDescription != null && !textDescription.isEmpty()) {
                sb.append(",\"textDescription\":\"").append(escape(textDescription)).append("\"");
            }
            if (htmlDescription != null && !htmlDescription.isEmpty()) {
                sb.append(",\"htmlDescription\":\"").append(escape(htmlDescription)).append("\"");
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                sb.append(",\"image\":\"").append(escape(imageUrl)).append("\"");
            }
            sb.append("}");
            apiService.createProductLine(sb.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Product line created successfully.");
            return "redirect:/productlines";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Creation failed: " + e.getMessage());
            return "redirect:/productlines/new";
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
