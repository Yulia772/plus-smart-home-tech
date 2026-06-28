package ru.yandex.practicum.order.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.delivery.DeliveryState;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.order.OrderState;
import ru.yandex.practicum.interactionapi.order.ProductReturnRequest;
import ru.yandex.practicum.interactionapi.payment.PaymentDto;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.interactionapi.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.order.client.DeliveryClient;
import ru.yandex.practicum.order.client.PaymentClient;
import ru.yandex.practicum.order.client.WarehouseClient;
import ru.yandex.practicum.order.exception.BadRequestException;
import ru.yandex.practicum.order.mapper.AddressMapper;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.model.OrderEntity;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWorkflowServiceImpl implements OrderWorkflowService {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final AddressMapper addressMapper;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        if (request == null || request.getOrderId() == null
                || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException("Передано неверное значение");
        }
        OrderEntity order = orderService.getOrderOrThrow(request.getOrderId());

        Map<UUID, Long> returnedProducts = request.getProducts();
        Map<UUID, Long> orderProducts = order.getProducts();

        for (Map.Entry<UUID, Long> entry : returnedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            if (productId == null) {
                throw new BadRequestException("Id товара не может быть пустым");
            }
            if (quantity == null || quantity <= 0) {
                throw new BadRequestException("Количество товара должно быть положительным");
            }
            if (!orderProducts.containsKey(productId)) {
                throw new BadRequestException("Передано неверное значение");
            }
            Long orderedQuantity = orderProducts.get(productId);
            if (quantity > orderedQuantity) {
                throw new BadRequestException("Передано неверное значение");
            }
        }
        if (order.getState() != OrderState.COMPLETED) {
            throw new BadRequestException("Возврат для этого заказа невозможен");
        }
        warehouseClient.acceptReturn(returnedProducts);
        order.setState(OrderState.PRODUCT_RETURNED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Оформлен возврат заказа: orderId={}", savedOrder.getOrderId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);

        if (order.getState() == OrderState.ON_PAYMENT) {
            return orderMapper.toDto(order);
        }
        if (order.getState() != OrderState.ASSEMBLED) {
            throw new BadRequestException("Заказ нельзя оплатить в текущем статусе");
        }
        if (order.getPaymentId() != null) {
            throw new BadRequestException("Оплата для заказа уже создана");
        }
        if (order.getTotalPrice() == null) {
            throw new BadRequestException("Стоимость заказа еще не рассчитана");
        }
        log.info("Создание оплаты для заказа: orderId={}", orderId);

        OrderDto orderDto = orderMapper.toDto(order);
        PaymentDto paymentDto = paymentClient.payment(orderDto);

        order.setPaymentId(paymentDto.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Заказ отправлен на оплату: orderId={}, paymentId={}",
                savedOrder.getOrderId(), savedOrder.getPaymentId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto paymentSuccessful(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getPaymentId() == null) {
            throw new BadRequestException("Оплата для заказа еще не создана");
        }
        if (order.getState() != OrderState.ON_PAYMENT) {
            throw new BadRequestException("Успешная оплата невозможна для заказа в текущем статусе");
        }

        paymentClient.paymentSuccess(order.getPaymentId());
        order.setState(OrderState.PAID);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Заказ успешно оплачен: orderId={}, paymentId={}",
                savedOrder.getOrderId(), savedOrder.getPaymentId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getPaymentId() == null) {
            throw new BadRequestException("Оплата для заказа еще не создана");
        }
        if (order.getState() != OrderState.ON_PAYMENT) {
            throw new BadRequestException("Ошибка оплаты невозможна для заказа в текущем статусе");
        }

        paymentClient.paymentFailed(order.getPaymentId());
        order.setState(OrderState.PAYMENT_FAILED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Оплата заказа завершилась ошибкой: orderId={}, paymentId={}",
                savedOrder.getOrderId(), savedOrder.getPaymentId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);

        if (order.getDeliveryId() == null) {
            throw new BadRequestException("Доставка для заказа еще не рассчитана");
        }
        if (order.getState() != OrderState.PAID) {
            throw new BadRequestException("Заказ еще не оплачен");
        }

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(orderId);
        request.setDeliveryId(order.getDeliveryId());

        warehouseClient.shippedToDelivery(request);
        deliveryClient.deliveryPicked(orderId);

        order.setState(OrderState.ON_DELIVERY);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Заказ передан в доставку: orderId={}, deliveryId={}",
                savedOrder.getOrderId(), savedOrder.getDeliveryId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getDeliveryId() == null) {
            throw new BadRequestException("Доставка для заказа еще не создана");
        }
        if (order.getState() != OrderState.ON_DELIVERY) {
            throw new BadRequestException("Ошибка доставки заказа невозможна для заказа в текущем статусе");
        }
        deliveryClient.deliveryFailed(orderId);
        order.setState(OrderState.DELIVERY_FAILED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Доставка заказа завершилась ошибкой: orderId={}, deliveryId={}",
                savedOrder.getOrderId(), savedOrder.getDeliveryId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getState() != OrderState.ON_DELIVERY) {
            throw new BadRequestException("Заказ еще не находится в доставке");
        }
        deliveryClient.deliverySuccessful(orderId);
        order.setState(OrderState.COMPLETED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Заказ завершен: orderId={}", savedOrder.getOrderId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getTotalPrice() != null) {
            return orderMapper.toDto(order);
        }
        if (order.getState() != OrderState.ASSEMBLED) {
            throw new BadRequestException("Стоимость заказа нельзя рассчитать в текущем статусе");
        }
        if (order.getDeliveryPrice() == null) {
            throw new BadRequestException("Стоимость доставки еще не рассчитана");
        }

        OrderDto orderDto = orderMapper.toDto(order);
        BigDecimal productPrice = paymentClient.productCost(orderDto);
        order.setProductPrice(productPrice);

        OrderDto dtoWithProductPrice = orderMapper.toDto(order);
        BigDecimal totalPrice = paymentClient.getTotalCost(dtoWithProductPrice);
        order.setTotalPrice(totalPrice);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Рассчитана стоимость заказа: orderId={}, productPrice={}, deliveryPrice={}, totalPrice={}",
                savedOrder.getOrderId(),
                savedOrder.getProductPrice(),
                savedOrder.getDeliveryPrice(),
                savedOrder.getTotalPrice());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);

        if (order.getDeliveryPrice() != null) {
            return orderMapper.toDto(order);
        }
        if (order.getState() != OrderState.ASSEMBLED) {
            throw new BadRequestException("Сборка заказа еще не подтверждена складом");
        }
        if (order.getFragile() == null ||
                order.getDeliveryVolume() == null ||
                order.getDeliveryWeight() == null) {
            throw new BadRequestException("Доставка не может быть рассчитана");
        }
        if (order.getDeliveryId() == null) {
            AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

            DeliveryDto deliveryDto = new DeliveryDto();
            UUID deliveryId = UUID.randomUUID();
            deliveryDto.setDeliveryId(deliveryId);
            deliveryDto.setOrderId(orderId);
            deliveryDto.setFromAddress(warehouseAddress);
            deliveryDto.setToAddress(addressMapper.toDto(order.getToAddress()));
            deliveryDto.setDeliveryState(DeliveryState.CREATED);

            DeliveryDto plannedDelivery = deliveryClient.planDelivery(deliveryDto);
            order.setDeliveryId(plannedDelivery.getDeliveryId());
        }
        OrderDto orderDto = orderMapper.toDto(order);
        BigDecimal price = deliveryClient.deliveryCost(orderDto);
        order.setDeliveryPrice(price);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Рассчитана стоимость доставки: orderId={}, deliveryId={}, deliveryPrice={}",
                savedOrder.getOrderId(),
                savedOrder.getDeliveryId(),
                savedOrder.getDeliveryPrice());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getState() != OrderState.NEW) {
            throw new BadRequestException("Заказ нельзя передать на сборку");
        }
        log.info("Передача заказа на сборку: orderId={}", orderId);

        Map<UUID, Long> products = order.getProducts();
        AssemblyProductsForOrderRequest orderRequest = new AssemblyProductsForOrderRequest();
        orderRequest.setOrderId(orderId);
        orderRequest.setProducts(products);

        BookedProductsDto productsDto = warehouseClient.assemblyProductsForOrder(orderRequest);
        order.setDeliveryWeight(productsDto.getDeliveryWeight());
        order.setDeliveryVolume(productsDto.getDeliveryVolume());
        order.setFragile(productsDto.getFragile());
        order.setState(OrderState.ASSEMBLED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Заказ собран: orderId={}", savedOrder.getOrderId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = orderService.getOrderOrThrow(orderId);
        if (order.getState() != OrderState.NEW) {
            throw new BadRequestException("Сборку нельзя отменить для заказа в текущем статусе");
        }
        order.setState(OrderState.ASSEMBLY_FAILED);

        OrderEntity savedOrder = orderService.save(order);
        log.info("Сборка заказа завершилась ошибкой: orderId={}", savedOrder.getOrderId());
        return orderMapper.toDto(savedOrder);
    }
}
