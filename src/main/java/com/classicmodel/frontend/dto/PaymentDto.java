package com.classicmodel.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {
    private PaymentIdDto id;
    private CustomerDto customer;
    private LocalDate paymentDate;
    private BigDecimal amount;

    public PaymentIdDto getId() { return id; }
    public void setId(PaymentIdDto id) { this.id = id; }
    public CustomerDto getCustomer() { return customer; }
    public void setCustomer(CustomerDto customer) { this.customer = customer; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentIdDto {
        private Integer customerNumber;
        private String checkNumber;

        public Integer getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(Integer customerNumber) { this.customerNumber = customerNumber; }
        public String getCheckNumber() { return checkNumber; }
        public void setCheckNumber(String checkNumber) { this.checkNumber = checkNumber; }
    }
}
