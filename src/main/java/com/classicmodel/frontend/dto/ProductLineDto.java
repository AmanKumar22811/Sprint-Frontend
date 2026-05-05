package com.classicmodel.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductLineDto {
    private String productLine;
    private String textDescription;
    private String htmlDescription;
    private String imageUrl;

    public String getProductLine() { return productLine; }
    public void setProductLine(String productLine) { this.productLine = productLine; }
    public String getTextDescription() { return textDescription; }
    public void setTextDescription(String textDescription) { this.textDescription = textDescription; }
    public String getHtmlDescription() { return htmlDescription; }
    public void setHtmlDescription(String htmlDescription) { this.htmlDescription = htmlDescription; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
