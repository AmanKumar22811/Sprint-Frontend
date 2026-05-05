package com.classicmodel.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {
    private String productCode;
    private String productName;
    private String productScale;
    private String productVendor;
    private String productDescription;
    private Short quantityInStock;
    private BigDecimal buyPrice;
    private BigDecimal MSRP;
    private ProductLineDto productLineEntity;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductScale() { return productScale; }
    public void setProductScale(String productScale) { this.productScale = productScale; }
    public String getProductVendor() { return productVendor; }
    public void setProductVendor(String productVendor) { this.productVendor = productVendor; }
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public Short getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Short quantityInStock) { this.quantityInStock = quantityInStock; }
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }
    public BigDecimal getMSRP() { return MSRP; }
    public void setMSRP(BigDecimal MSRP) { this.MSRP = MSRP; }
    public ProductLineDto getProductLineEntity() { return productLineEntity; }
    public void setProductLineEntity(ProductLineDto productLineEntity) { this.productLineEntity = productLineEntity; }
}
