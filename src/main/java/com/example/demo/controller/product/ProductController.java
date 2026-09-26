package com.example.demo.controller.product;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.product.ProductPatchRequest;
import com.example.demo.dto.request.product.ProductRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.dto.response.product.ProductResponse;
import com.example.demo.service.product.ProductService;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<ProductResponse> productPage = productService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(productPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse savedProduct = productService.create(request);
        return ResponseEntity.created(URI.create("/products/" + savedProduct.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedProduct, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable @Min(1) Long id) {
        ProductResponse product = productService.getById(id);
        return ApiResponse.success(product, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody ProductRequest input) {
        ProductResponse updated = productService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody ProductPatchRequest input) {
        ProductResponse edited = productService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        productService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<ProductResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @Min(0) Double minPrice,
            @RequestParam(required = false) @Min(0) Double maxPrice,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<ProductResponse> searchResult = productService.search(name, minPrice, maxPrice, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
