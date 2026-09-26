package com.example.demo.service.product.serviceImpl;

import com.example.demo.config.CacheConfig;
import com.example.demo.dto.patch.product.ProductPatchRequest;
import com.example.demo.dto.request.product.ProductRequest;
import com.example.demo.dto.response.product.ProductResponse;
import com.example.demo.entity.product.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.product.ProductRepository;
import com.example.demo.service.product.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse getById(Long id) {
        return toResponse(findProductById(id));
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product product = modelMapper.map(request, Product.class);
        return toResponse(productRepository.save(product));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse update(Long id, ProductRequest request) {
        Product existingProduct = findProductById(id);
        modelMapper.map(request, existingProduct);
        return toResponse(productRepository.save(existingProduct));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse edit(Long id, ProductPatchRequest request) {
        Product existingProduct = findProductById(id);

        if (request.productName() != null) {
            existingProduct.setProductName(request.productName());
        }
        if (request.description() != null) {
            existingProduct.setDescription(request.description());
        }
        if (request.unitPrice() != null) {
            existingProduct.setUnitPrice(request.unitPrice());
        }

        return toResponse(productRepository.save(existingProduct));
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public void deleteById(Long id) {
        productRepository.delete(findProductById(id));
    }

    @Override
    public Page<ProductResponse> search(String name, Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.searchProducts(name, minPrice, maxPrice, pageable)
                .map(this::toResponse);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return modelMapper.map(product, ProductResponse.class);
    }
}
