package ru.yandex.practicum.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.interactionapi.exception.BadRequestException;
import ru.yandex.practicum.interactionapi.exception.NotFoundException;
import ru.yandex.practicum.interactionapi.store.*;

import ru.yandex.practicum.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("Запрос товаров: category={}, pageable={}", category, pageable);

        Page<Product> products = productRepository.findByProductCategory(
                category,
                pageable
        );
        log.info("Найдено товаров: totalElements={}, totalPages={}, contentSize={}",
                products.getTotalElements(), products.getTotalPages(), products.getContent().size());
        return products.map(productMapper::toDto);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        if (productDto.getProductId() == null) {
            productDto.setProductId(UUID.randomUUID());
        }
        Product product = productMapper.toProduct(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();
        if (productId == null) {
            log.warn("Невозможно обновить продукт без id: product={}", productDto);
            throw new BadRequestException("Невозможно обновить продукт без id");
        }
        getProductOrThrow(productId);
        Product product = productMapper.toProduct(productDto);
        Product updateProduct = productRepository.save(product);
        return productMapper.toDto(updateProduct);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        Product foundProduct = getProductOrThrow(productId);

        if (foundProduct.getProductState() == ProductState.DEACTIVATE) {
            return false;
        }
        foundProduct.setProductState(ProductState.DEACTIVATE);
        productRepository.save(foundProduct);
        return true;
    }

    @Override
    public Boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
        if (productId == null) {
            throw new BadRequestException("Идентификатор товара не может быть пустым");
        }
        if (quantityState == null) {
            throw new BadRequestException("Статус количества товара не может быть пустым");
        }
        Product product = getProductOrThrow(productId);
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        Product product = getProductOrThrow(productId);
        return productMapper.toDto(product);
    }

    private Product getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                        .orElseThrow(() -> {
                            log.warn("Продукт с id={} не найден", productId);
                            return new NotFoundException("Продукт с id=" + productId + " не найден");
                        });
    }
}
