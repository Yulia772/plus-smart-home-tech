package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.calculator.DeliveryCostCalculator;
import ru.yandex.practicum.delivery.client.OrderClient;
import ru.yandex.practicum.delivery.client.WarehouseClient;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.model.DeliveryEntity;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.interactionapi.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.delivery.DeliveryState;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;
import ru.yandex.practicum.interactionapi.order.OrderDto;
import ru.yandex.practicum.interactionapi.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.warehouse.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;
    private final DeliveryCostCalculator deliveryCostCalculator;


    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto dto) {
        if (dto == null) {
            throw new BadRequestException("Переданы неверные данные");
        }
        if (dto.getDeliveryId() == null) {
            throw new BadRequestException("Переданы неверные данные");
        }
        if (dto.getOrderId() == null) {
            throw new BadRequestException("Не передано id заказа");
        }
        validateAddress(dto.getFromAddress(), "Адрес склада отгрузки");
        validateAddress(dto.getToAddress(), "Адрес доставки заказа");

        if (dto.getDeliveryState() == null) {
            throw new BadRequestException("Не передан статус доставки");
        }
        if (dto.getDeliveryState() != DeliveryState.CREATED) {
            throw new BadRequestException("Передано некорректное значение");
        }
        if (deliveryRepository.existsById(dto.getDeliveryId())) {
            throw new BadRequestException("Доставка с таким id уже существует");
        }
        if (deliveryRepository.existsByOrderId(dto.getOrderId())) {
            throw new BadRequestException("Доставка для этого заказа уже существует");
        }

        DeliveryEntity delivery = new DeliveryEntity();

        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setOrderId(dto.getOrderId());
        delivery.setDeliveryState(dto.getDeliveryState());
        delivery.setFromAddress(deliveryMapper.toEntity(dto.getFromAddress()));
        delivery.setToAddress(deliveryMapper.toEntity(dto.getToAddress()));

        DeliveryEntity savedEntity = deliveryRepository.save(delivery);
        log.info("Создана доставка: deliveryId={}, orderId={}, state={}",
                savedEntity.getDeliveryId(),
                savedEntity.getOrderId(),
                savedEntity.getDeliveryState());

        return deliveryMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID orderId) {
        DeliveryEntity delivery = getDeliveryByOrderId(orderId);

        if (delivery.getDeliveryId() == null) {
            throw new BadRequestException("У доставки не указан id");
        }
        if (delivery.getDeliveryState() == DeliveryState.DELIVERED) {
            log.info("Доставка уже завершена успешно: orderId={}, deliveryId={}",
                    orderId, delivery.getDeliveryId());
            return;
        }
        if (delivery.getDeliveryState() == DeliveryState.CREATED) {
            throw new BadRequestException("Доставка еще не начата, успешная доставка заказа невозможна");
        }
        if (delivery.getDeliveryState() != DeliveryState.IN_PROGRESS) {
            throw new BadRequestException("Успешная доставка заказа в данном статусе невозможна");
        }
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);
        log.info("Доставка успешно завершена: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());

        orderClient.complete(orderId);

        log.info("Сервис заказов уведомлен об успешной доставке заказа: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID orderId) {
        DeliveryEntity delivery = getDeliveryByOrderId(orderId);

        if (delivery.getDeliveryId() == null) {
            throw new BadRequestException("У доставки не указан id");
        }
        if (delivery.getDeliveryState() == DeliveryState.IN_PROGRESS) {
            log.info("Доставка уже находится в процессе: orderId={}, deliveryId={}",
                    orderId, delivery.getDeliveryId());
            return;
        }
        if (delivery.getDeliveryState() != DeliveryState.CREATED) {
            throw new BadRequestException("Доставка в некорректном статусе");
        }
        if (delivery.getDeliveryWeight() == null
                || delivery.getDeliveryVolume() == null
                || delivery.getFragile() == null) {
            throw new BadRequestException("Доставка еще не рассчитана");
        }

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);
        log.info("Доставка переведена в процесс: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setDeliveryId(savedDelivery.getDeliveryId());
        request.setOrderId(orderId);

        warehouseClient.shippedToDelivery(request);
        orderClient.delivery(orderId);

        log.info("Склад и сервис заказов уведомлены о передаче доставки: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        DeliveryEntity delivery = getDeliveryByOrderId(orderId);

        if (delivery.getDeliveryId() == null) {
            throw new BadRequestException("У доставки не указан id");
        }
        if (delivery.getDeliveryState() == DeliveryState.FAILED) {
            log.info("Доставка уже переведена в статус FAILED: orderId={}, deliveryId={}",
                    orderId, delivery.getDeliveryId());
            return;
        }
        if (delivery.getDeliveryState() != DeliveryState.IN_PROGRESS) {
            throw new BadRequestException("Доставка в некорректном статусе");
        }
        delivery.setDeliveryState(DeliveryState.FAILED);
        DeliveryEntity savedDelivery = deliveryRepository.save(delivery);
        log.info("Доставка завершена неуспешно: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());

        orderClient.deliveryFailed(orderId);
        log.info("Сервис заказов уведомлен о неудаче доставки: orderId={}, deliveryId={}",
                orderId, savedDelivery.getDeliveryId());
    }

    @Override
    @Transactional
    public BigDecimal deliveryCost(OrderDto dto) {
        if (dto == null) {
            throw new BadRequestException("Переданы неверные данные");
        }
        if (dto.getDeliveryId() == null) {
            throw new BadRequestException("Переданы неверные данные");
        }
        DeliveryEntity delivery = getDeliveryByOrderId(dto.getOrderId());

        if (!dto.getDeliveryId().equals(delivery.getDeliveryId())) {
            throw new BadRequestException("Переданы некорректные данные");
        }
        if (dto.getDeliveryVolume() == null || dto.getDeliveryVolume() <= 0) {
            throw new BadRequestException("Отсутствуют данные по объему доставки");
        }
        if (dto.getDeliveryWeight() == null || dto.getDeliveryWeight() <= 0) {
            throw new BadRequestException("Отсутствуют данные по весу доставки");
        }
        if (dto.getFragile() == null) {
            throw new BadRequestException("Отсутствуют данные по хрупкости доставки");
        }
        if (delivery.getDeliveryState() != DeliveryState.CREATED) {
            throw new BadRequestException("Стоимость доставки нельзя рассчитать в текущем статусе");
        }
        delivery.setDeliveryVolume(dto.getDeliveryVolume());
        delivery.setDeliveryWeight(dto.getDeliveryWeight());
        delivery.setFragile(dto.getFragile());

        BigDecimal deliveryCost = deliveryCostCalculator.calculate(delivery);

        deliveryRepository.save(delivery);

        log.info("Рассчитана стоимость доставки: orderId={}, deliveryId={}, deliveryCost={}",
                delivery.getOrderId(),
                delivery.getDeliveryId(),
                deliveryCost);

        return deliveryCost;
    }

    private void validateAddress(AddressDto address, String addressName) {
        if (address == null) {
            throw new BadRequestException(addressName + " не указан");
        }
        if (address.getCountry() == null || address.getCountry().isBlank()) {
            throw new BadRequestException(addressName + ": не указана страна");
        }
        if (address.getCity() == null || address.getCity().isBlank()) {
            throw new BadRequestException(addressName + ": не указан город");
        }
        if (address.getStreet() == null || address.getStreet().isBlank()) {
            throw new BadRequestException(addressName + ": не указана улица");
        }
        if (address.getHouse() == null || address.getHouse().isBlank()) {
            throw new BadRequestException(addressName + ": не указан дом");
        }
    }

    private DeliveryEntity getDeliveryByOrderId(UUID orderId) {
        if (orderId == null) {
            throw new BadRequestException("Переданы неверные данные");
        }
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Доставка по переданному orderId не найдена"));
    }
}
