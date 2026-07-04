package ru.yandex.practicum.delivery.calculator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.delivery.model.DeliveryAddress;
import ru.yandex.practicum.delivery.model.DeliveryEntity;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class DeliveryCostCalculator {

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(5.0);
    private static final BigDecimal ADDRESS_1_MULTIPLIER = BigDecimal.valueOf(1);
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2);
    private static final BigDecimal FRAGILE_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal ADDRESS_DIFFERENCE_RATE = BigDecimal.valueOf(0.2);

    public BigDecimal calculate(DeliveryEntity delivery) {

        BigDecimal current;

        if (isAddressContains(delivery.getFromAddress(), "ADDRESS_1")) {
            current = BASE_COST.add(BASE_COST.multiply(ADDRESS_1_MULTIPLIER));
        } else if (isAddressContains(delivery.getFromAddress(), "ADDRESS_2")) {
            current = BASE_COST.add(BASE_COST.multiply(ADDRESS_2_MULTIPLIER));
        } else {
            throw new BadRequestException("Неизвестный адрес склада");
        }

        if (delivery.getFragile()) {
            current = current.add(current.multiply(FRAGILE_RATE));
        }

        BigDecimal weightCost = BigDecimal.valueOf(delivery.getDeliveryWeight())
                .multiply(WEIGHT_RATE);
        current = current.add(weightCost);

        BigDecimal volumeCost = BigDecimal.valueOf(delivery.getDeliveryVolume())
                .multiply(VOLUME_RATE);
        current = current.add(volumeCost);

        if (!Objects.equals(delivery.getFromAddress().getStreet(),
                delivery.getToAddress().getStreet())) {
            current = current.add(current.multiply(ADDRESS_DIFFERENCE_RATE));
        }
        return current;
    }

    private boolean isAddressContains(DeliveryAddress address, String addressName) {
        if (address == null) {
            throw new BadRequestException("Адрес не указан");
        }
        return contains(address.getCountry(), addressName)
                || contains(address.getCity(), addressName)
                || contains(address.getStreet(), addressName)
                || contains(address.getHouse(), addressName)
                || contains(address.getFlat(), addressName);
    }

    private boolean contains(String value, String expected) {
        return value != null && value.contains(expected);
    }
}
