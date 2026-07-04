package ru.yandex.practicum.interactionapi.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
public class PaymentDto {
    private UUID paymentId;
    private BigDecimal totalPayment;
    private BigDecimal deliveryTotal;
    private BigDecimal feeTotal;
}
