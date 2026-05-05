package com.classicmodel.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetailDto {
    private OrderDetailIdDto id;
    private ProductDto product;
    private Integer quantityOrdered;
    private BigDecimal priceEach;
    private Integer orderLineNumber;

    public OrderDetailIdDto getId() { return id; }
    public void setId(OrderDetailIdDto id) { this.id = id; }
    public ProductDto getProduct() { return product; }
    public void setProduct(ProductDto product) { this.product = product; }
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public BigDecimal getPriceEach() { return priceEach; }
    public void setPriceEach(BigDecimal priceEach) { this.priceEach = priceEach; }
    public Integer getOrderLineNumber() { return orderLineNumber; }
    public void setOrderLineNumber(Integer orderLineNumber) { this.orderLineNumber = orderLineNumber; }

    public BigDecimal getLineTotal() {
        if (quantityOrdered != null && priceEach != null) {
            return priceEach.multiply(BigDecimal.valueOf(quantityOrdered));
        }
        return BigDecimal.ZERO;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderDetailIdDto {
        private Integer orderNumber;
        private String productCode;

        public Integer getOrderNumber() { return orderNumber; }
        public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
    }
}
