package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.interactionapi.store.ProductDto;
import ru.yandex.practicum.payment.client.OrderClient;
import ru.yandex.practicum.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.model.PaymentEntity;
import ru.yandex.practicum.payment.model.PaymentState;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;
    private final PaymentMapper paymentMapper;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);

    @Override
    @Transactional
    public PaymentDto payment(OrderDto dto) {
        validateOrderDto(dto);
        if (dto.getProductPrice() == null) {
            throw new BadRequestException("Не рассчитана стоимость товаров");
        }
        if (dto.getDeliveryPrice() == null) {
            throw new BadRequestException("Не рассчитана стоимость доставки");
        }
        if (dto.getTotalPrice() == null) {
            throw new BadRequestException("Не рассчитана полная стоимость");
        }
        BigDecimal productPrice = dto.getProductPrice();
        BigDecimal feeTotal = productPrice.multiply(TAX_RATE);

        PaymentEntity payment = new PaymentEntity();

        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(dto.getOrderId());
        payment.setProductTotal(dto.getProductPrice());
        payment.setDeliveryTotal(dto.getDeliveryPrice());
        payment.setFeeTotal(feeTotal);
        payment.setTotalPayment(dto.getTotalPrice());
        payment.setState(PaymentState.PENDING);

        PaymentEntity savedEntity = paymentRepository.save(payment);

        log.info("Создана заявка на оплату: paymentId={}, orderId={}, totalPayment={}",
                savedEntity.getPaymentId(),
                savedEntity.getOrderId(),
                savedEntity.getTotalPayment());

        return paymentMapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto dto) {
        validateOrderDto(dto);
        if (dto.getProductPrice() == null) {
            throw new BadRequestException("Не рассчитана стоимость товаров");
        }
        if (dto.getDeliveryPrice() == null) {
            throw new BadRequestException("Не рассчитана стоимость доставки");
        }

        BigDecimal productPrice = dto.getProductPrice();
        BigDecimal deliveryPrice = dto.getDeliveryPrice();
        BigDecimal feeTotal = productPrice.multiply(TAX_RATE);

        BigDecimal totalCost = productPrice
                .add(feeTotal)
                .add(deliveryPrice);

        log.info("Рассчитана стоимость заказа: orderId={}, totalCost={}",
                dto.getOrderId(), totalCost);

        return totalCost;
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        if (paymentId == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Оплата не найдена в базе"));
        if (payment.getOrderId() == null) {
            throw new BadRequestException("У оплаты не указан заказ");
        }

        if (payment.getState() == PaymentState.SUCCESS) {
            log.info("Оплата уже подтверждена: paymentId={}, orderId={}",
                    paymentId, payment.getOrderId());
            return;
        }
        if (payment.getState() == PaymentState.FAILED) {
            log.warn("Попытка подтвердить отклоненную оплату: paymentId={}, orderId={}",
                    paymentId, payment.getOrderId());
            throw new BadRequestException("Невозможно подтвердить платеж");
        }
        if (payment.getState() == PaymentState.PENDING) {
            payment.setState(PaymentState.SUCCESS);
        } else {
            throw new BadRequestException("Оплата находится в некорректном статусе");
        }
        paymentRepository.save(payment);
        log.info("Оплата подтверждена: paymentId={}, orderId={}",
                payment.getPaymentId(), payment.getOrderId());

        UUID orderId = payment.getOrderId();
        orderClient.paymentSuccessful(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto dto) {
        validateOrderDto(dto);
        Map<UUID, Long> products = dto.getProducts();

        if (products == null || products.isEmpty()) {
            throw  new BadRequestException("Переданы неверные данные");
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            if (productId == null) {
                throw new BadRequestException("Идентификатор товара не может быть пустым");
            }
            if (quantity == null || quantity <= 0) {
                throw new BadRequestException("Количество товара должно быть больше нуля");
            }
        }

        Set<UUID> productIds = products.keySet();
        List<ProductDto> productDtos = shoppingStoreClient.getProductsByIds(productIds);

        if (productDtos == null || productDtos.isEmpty()) {
            throw new NotFoundException("Товары не найдены");
        }

        Map<UUID, ProductDto> productsById = productDtos.stream()
                .collect(Collectors.toMap(ProductDto::getProductId, Function.identity()));

        if (productsById.size() != productIds.size()) {
            throw new NotFoundException("Один или несколько товаров не найдены");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto productDto = productsById.get(productId);
            if (productDto == null) {
                throw new NotFoundException("Товар не найден");
            }
            BigDecimal price = productDto.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Не удалось получить корректную цену товара");
            }
            BigDecimal productTotal = price.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(productTotal);
        }
        log.info("Рассчитана стоимость товаров заказа: orderId={}, productCost={}",
                dto.getOrderId(), totalPrice);

        return totalPrice;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        if (paymentId == null) {
            throw new BadRequestException("Передано неверное значение");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Оплата не найдена в базе"));
        if (payment.getOrderId() == null) {
            throw new BadRequestException("У оплаты не указан заказ");
        }
        if (payment.getState() == PaymentState.FAILED) {
            log.info("Оплата уже отклонена: paymentId={}, orderId={}",
                    paymentId, payment.getOrderId());
            return;
        }
        if (payment.getState() == PaymentState.SUCCESS) {
            log.warn("Попытка отклонить подтвержденную оплату: paymentId={}, orderId={}",
                    paymentId, payment.getOrderId());
            throw new BadRequestException("Невозможно изменить статус платежа");
        }
        if (payment.getState() == PaymentState.PENDING) {
            payment.setState(PaymentState.FAILED);
        } else {
            throw new BadRequestException("Оплата находится в некорректном статусе");
        }

        paymentRepository.save(payment);
        log.info("Оплата отклонена: paymentId={}, orderId={}",
                payment.getPaymentId(), payment.getOrderId());

        UUID orderId = payment.getOrderId();
        orderClient.paymentFailed(orderId);
    }

    private void validateOrderDto(OrderDto dto) {
        if (dto == null) {
            throw  new BadRequestException("Переданы неверные данные");
        }
        if (dto.getOrderId() == null) {
            throw  new BadRequestException("Переданы неверные данные");
        }
    }
}
