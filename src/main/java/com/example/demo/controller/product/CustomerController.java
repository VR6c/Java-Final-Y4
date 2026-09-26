package com.example.demo.controller.product;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.product.CustomerPatchRequest;
import com.example.demo.dto.request.product.CustomerRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.product.CustomerResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.service.product.CustomerService;
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
@RequestMapping("/customers")
@Validated
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<CustomerResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<CustomerResponse> customerPage = customerService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(customerPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse savedCustomer = customerService.create(request);
        return ResponseEntity.created(URI.create("/customers/" + savedCustomer.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedCustomer, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable @Min(1) Long id) {
        CustomerResponse customer = customerService.getById(id);
        return ApiResponse.success(customer, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody CustomerRequest input) {
        CustomerResponse updated = customerService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<CustomerResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody CustomerPatchRequest input) {
        CustomerResponse edited = customerService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        customerService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<CustomerResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<CustomerResponse> searchResult = customerService.search(name, phone, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
