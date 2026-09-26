package com.example.demo.controller.product;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.product.ProductOrderPatchRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.dto.response.product.ProductOrderResponse;
import com.example.demo.service.product.ProductOrderService;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-orders")
@Validated
public class ProductOrderController {

    private final ProductOrderService productOrderService;

    public ProductOrderController(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<ProductOrderResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<ProductOrderResponse> poPage = productOrderService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(poPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductOrderResponse> getById(@PathVariable @Min(1) Long id) {
        ProductOrderResponse po = productOrderService.getById(id);
        return ApiResponse.success(po, AppConstants.SUCCESS_RETRIEVE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductOrderResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody ProductOrderPatchRequest input) {
        ProductOrderResponse edited = productOrderService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        productOrderService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
