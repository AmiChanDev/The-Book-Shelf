package hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 *
 * @author AmiChan
 */
@Entity
@Table(name = "orders")
public class Order implements Serializable {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "order_date")
    private Timestamp orderDate;

    public Order() {
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    @PrePersist
    protected void onCreate() {
        this.setOrderDate(new Timestamp(System.currentTimeMillis()));
    }

    public void setId(Integer id) {
        if (id != null) {
            this.id = id.longValue();
        }
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public void setId(long id) {
        this.id = id;
    }
}
