package ru.yandex.practicum.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.swing.plaf.basic.BasicIconFactory;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@ToString
public class PaymentEntity {
    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_total", nullable = false)
    private BigDecimal productTotal;

    @Column(name = "delivery_total", nullable = false)
    private BigDecimal deliveryTotal;

    @Column(name = "fee_total", nullable = false)
    private BigDecimal feeTotal;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PaymentState state;
}
