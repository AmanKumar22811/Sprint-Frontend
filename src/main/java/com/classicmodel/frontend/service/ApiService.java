package com.classicmodel.frontend.service;

import com.classicmodel.frontend.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;

    public static final int PAGE_SIZE = 20;

    @Autowired
    public ApiService(RestTemplate restTemplate, ObjectMapper objectMapper,
                      @Qualifier("apiBaseUrl") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl;
    }

    // ---------- PageResult ----------

    public static class PageResult<T> {
        public final List<T> items;
        public final int currentPage;
        public final int totalPages;
        public final long totalElements;

        public PageResult(List<T> items, int currentPage, int totalPages, long totalElements) {
            this.items = items;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }
    }

    // ---------- helpers ----------

    private <T> List<T> extractEmbedded(String json, String key, TypeReference<List<T>> ref) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode embedded = root.get("_embedded");
            if (embedded == null || !embedded.has(key)) return new ArrayList<>();
            return objectMapper.convertValue(embedded.get(key), ref);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private <T> PageResult<T> fetchPage(String path, String embeddedKey,
                                         TypeReference<List<T>> ref, int page) {
        try {
            String url = apiBaseUrl + path + "?page=" + page + "&size=" + PAGE_SIZE;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode embedded = root.get("_embedded");
            List<T> items = new ArrayList<>();
            if (embedded != null && embedded.has(embeddedKey)) {
                items = objectMapper.convertValue(embedded.get(embeddedKey), ref);
            }
            JsonNode pageNode = root.path("page");
            int totalPages = pageNode.path("totalPages").asInt(1);
            long totalElements = pageNode.path("totalElements").asLong(0);
            return new PageResult<>(items, page, totalPages, totalElements);
        } catch (Exception e) {
            return new PageResult<>(new ArrayList<>(), 0, 1, 0);
        }
    }

    /** Fetch a full (unpaged) list — used for dropdowns and detail-page lookups. */
    private String getList(String path) {
        return restTemplate.getForObject(apiBaseUrl + path + "?size=1000", String.class);
    }

    /** Fetch a single entity WITHOUT projection so all fields are returned. */
    private String getSingle(String path) {
        return restTemplate.getForObject(apiBaseUrl + path, String.class);
    }

    /** Fetch a single entity WITH a named projection. */
    private String getSingleProjected(String path, String projection) {
        return restTemplate.getForObject(apiBaseUrl + path + "?projection=" + projection, String.class);
    }

    private void patchEntity(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(apiBaseUrl + path, HttpMethod.PATCH,
                new HttpEntity<>(body, headers), String.class);
    }

    private void postEntity(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForObject(apiBaseUrl + path, new HttpEntity<>(body, headers), String.class);
    }

    // ---------- EMPLOYEES ----------

    public PageResult<EmployeeDto> getPagedEmployees(int page) {
        return fetchPage("/employees", "employees",
                new TypeReference<List<EmployeeDto>>() {}, page);
    }

    public List<EmployeeDto> getAllEmployees() {
        String json = getList("/employees");
        return extractEmbedded(json, "employees", new TypeReference<List<EmployeeDto>>() {});
    }

    public EmployeeDto getEmployee(Integer id) {
        try {
            // Fetch plain entity — projection flattens _links so manager/office would be null
            String json = getSingle("/employees/" + id);
            EmployeeDto emp = objectMapper.readValue(json, EmployeeDto.class);

            // Manager comes back as a _links reference — fetch the sub-resource directly
            try {
                String managerJson = getSingle("/employees/" + id + "/manager");
                EmployeeDto.ManagerDto manager = objectMapper.readValue(managerJson, EmployeeDto.ManagerDto.class);
                emp.setManager(manager);
            } catch (Exception ignored) {
                // 404 means top-level employee — manager stays null
            }

            // Office is also a _links reference — fetch the sub-resource directly
            try {
                String officeJson = getSingle("/employees/" + id + "/office");
                OfficeDto office = objectMapper.readValue(officeJson, OfficeDto.class);
                emp.setOffice(office);
            } catch (Exception ignored) {
                // No office assigned
            }

            return emp;
        } catch (Exception e) {
            throw new RuntimeException("Employee not found: " + id, e);
        }
    }

    /** Customers whose sales rep is the given employee number. */
    public List<CustomerDto> getCustomersByEmployee(Integer employeeNumber) {
        try {
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/customers/search/findBySalesRepEmployee_EmployeeNumber"
                            + "?employeeNumber=" + employeeNumber + "&size=200", String.class);
            return extractEmbedded(json, "customers", new TypeReference<List<CustomerDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateEmployee(Integer id, String body) {
        patchEntity("/employees/" + id, body);
    }

    public void createEmployee(String body) {
        postEntity("/employees", body);
    }

    /** Employees at a given office — uses findByOffice_OfficeCode search method */
    public List<EmployeeDto> getEmployeesByOffice(String officeCode) {
        try {
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/employees/search/findByOffice_OfficeCode?officeCode=" + officeCode, String.class);
            return extractEmbedded(json, "employees", new TypeReference<List<EmployeeDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ---------- CUSTOMERS ----------

    public PageResult<CustomerDto> getPagedCustomers(int page) {
        return fetchPage("/customers", "customers",
                new TypeReference<List<CustomerDto>>() {}, page);
    }

    public List<CustomerDto> getAllCustomers() {
        String json = getList("/customers");
        return extractEmbedded(json, "customers", new TypeReference<List<CustomerDto>>() {});
    }

    public CustomerDto getCustomer(Integer id) {
        try {
            String json = getSingle("/customers/" + id);
            return objectMapper.readValue(json, CustomerDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Customer not found: " + id, e);
        }
    }

    public void updateCustomer(Integer id, String body) {
        patchEntity("/customers/" + id, body);
    }

    public void createCustomer(String body) {
        postEntity("/customers", body);
    }

    /** Orders for a specific customer */
    public List<OrderDto> getOrdersByCustomer(Integer customerId) {
        try {
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/orders/search/findByCustomer_CustomerNumber?customerNumber=" + customerId
                            + "&projection=orderExcerpt", String.class);
            return extractEmbedded(json, "orders", new TypeReference<List<OrderDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** Payments for a specific customer */
    public List<PaymentDto> getPaymentsByCustomer(Integer customerId) {
        try {
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/payments/search/findByCustomer_CustomerNumber?customerNumber=" + customerId, String.class);
            return extractEmbedded(json, "payments", new TypeReference<List<PaymentDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ---------- OFFICES ----------

    public PageResult<OfficeDto> getPagedOffices(int page) {
        return fetchPage("/offices", "offices",
                new TypeReference<List<OfficeDto>>() {}, page);
    }

    public List<OfficeDto> getAllOffices() {
        String json = getList("/offices");
        return extractEmbedded(json, "offices", new TypeReference<List<OfficeDto>>() {});
    }

    public OfficeDto getOffice(String code) {
        try {
            String json = getSingleProjected("/offices/" + code, "officeExcerpt");
            return objectMapper.readValue(json, OfficeDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Office not found: " + code, e);
        }
    }

    public void updateOffice(String code, String body) {
        patchEntity("/offices/" + code, body);
    }

    public void createOffice(String body) {
        postEntity("/offices", body);
    }

    // ---------- PRODUCTS ----------

    public List<ProductDto> getAllProducts() {
        String json = getList("/products");
        return extractEmbedded(json, "products", new TypeReference<List<ProductDto>>() {});
    }

    public ProductDto getProduct(String code) {
        try {
            String json = getSingleProjected("/products/" + code, "productExcerpt");
            return objectMapper.readValue(json, ProductDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Product not found: " + code, e);
        }
    }

    public void updateProduct(String code, String body) {
        patchEntity("/products/" + code, body);
    }

    public void createProduct(String body) {
        postEntity("/products", body);
    }

    /** Products in a product line */
    public List<ProductDto> getProductsByProductLine(String productLine) {
        try {
            String encoded = java.net.URLEncoder.encode(productLine, "UTF-8");
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/products/search/findByProductLineEntity_ProductLine?productLine=" + encoded, String.class);
            return extractEmbedded(json, "products", new TypeReference<List<ProductDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ---------- PRODUCT LINES ----------

    public PageResult<ProductLineDto> getPagedProductLines(int page) {
        return fetchPage("/productlines", "productLines",
                new TypeReference<List<ProductLineDto>>() {}, page);
    }

    public List<ProductLineDto> getAllProductLines() {
        String json = getList("/productlines");
        return extractEmbedded(json, "productLines", new TypeReference<List<ProductLineDto>>() {});
    }

    public ProductLineDto getProductLine(String name) {
        try {
            String json = getSingleProjected("/productlines/" + name, "productLineExcerpt");
            return objectMapper.readValue(json, ProductLineDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Product line not found: " + name, e);
        }
    }

    public void updateProductLine(String name, String body) {
        patchEntity("/productlines/" + name, body);
    }

    public void createProductLine(String body) {
        postEntity("/productlines", body);
    }

    // ---------- ORDERS ----------

    public PageResult<OrderDto> getPagedOrders(int page) {
        return fetchPage("/orders", "orders",
                new TypeReference<List<OrderDto>>() {}, page);
    }

    public List<OrderDto> getAllOrders() {
        String json = getList("/orders");
        return extractEmbedded(json, "orders", new TypeReference<List<OrderDto>>() {});
    }

    public OrderDto getOrder(Integer orderNumber) {
        try {
            String json = getSingleProjected("/orders/" + orderNumber, "orderExcerpt");
            return objectMapper.readValue(json, OrderDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Order not found: " + orderNumber, e);
        }
    }

    public void updateOrder(Integer orderNumber, String body) {
        patchEntity("/orders/" + orderNumber, body);
    }

    public void createOrder(String body) {
        postEntity("/orders", body);
    }

    public List<OrderDetailDto> getOrderDetails(Integer orderNumber) {
        try {
            String json = restTemplate.getForObject(
                    apiBaseUrl + "/orderdetails/search/findByOrder_OrderNumber?orderNumber=" + orderNumber
                            + "&projection=orderDetailExcerpt", String.class);
            List<OrderDetailDto> result = extractEmbedded(json, "orderDetails",
                    new TypeReference<List<OrderDetailDto>>() {});
            if (result.isEmpty()) {
                result = extractEmbedded(json, "orderdetails",
                        new TypeReference<List<OrderDetailDto>>() {});
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ---------- PAYMENTS ----------

    public List<PaymentDto> getAllPayments() {
        String json = getList("/payments");
        return extractEmbedded(json, "payments", new TypeReference<List<PaymentDto>>() {});
    }

    public PaymentDto getPayment(String customerNumber, String checkNumber) {
        try {
            String json = getSingle("/payments/" + customerNumber + "_" + checkNumber);
            return objectMapper.readValue(json, PaymentDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Payment not found: customer=" + customerNumber
                    + " check=" + checkNumber, e);
        }
    }

    public void createPayment(String body) {
        postEntity("/payments", body);
    }

    // ---------- ANALYTICS / COUNTS ----------

    public long countCustomers() {
        return countViaPage("/customers");
    }

    public long countOrders() {
        return countViaPage("/orders");
    }

    public long countProducts() {
        return countViaPage("/products");
    }

    public long countEmployees() {
        return countViaPage("/employees");
    }

    private long countViaPage(String path) {
        try {
            String json = restTemplate.getForObject(apiBaseUrl + path + "?size=1", String.class);
            JsonNode root = objectMapper.readTree(json);
            return root.path("page").path("totalElements").asLong(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
