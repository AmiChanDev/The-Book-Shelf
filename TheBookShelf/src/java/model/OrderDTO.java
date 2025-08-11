package model;


public class OrderDTO {
    private Long id;
    private String customerName;
    private String orderDate;
    private Double totalAmount;

    public OrderDTO(Long id, String customerName, String orderDate, Double totalAmount) {
        this.id = id;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }

}

