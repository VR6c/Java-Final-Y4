package com.example.demo.controller.school;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.patch.school.CardPatchRequest;
import com.example.demo.dto.request.school.CardRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.school.CardResponse;
import com.example.demo.dto.response.PaginationResponse;
import com.example.demo.service.school.CardService;
import com.example.demo.util.PaginationUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;

@Hidden
@RestController
@RequestMapping("/cards")
@Validated
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ApiResponse<PaginationResponse<CardResponse>> getAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {
        
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<CardResponse> cardPage = cardService.getAll(pageable);
        return ApiResponse.success(PaginationResponse.from(cardPage), AppConstants.SUCCESS_FETCH_ALL);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardResponse>> create(@Valid @RequestBody CardRequest cardRequest) {
        CardResponse savedCard = cardService.create(cardRequest);
        return ResponseEntity.created(URI.create("/cards/" + savedCard.getId()))
                .body(ApiResponse.success(HttpStatus.CREATED.value(), savedCard, AppConstants.SUCCESS_CREATE));
    }

    @GetMapping("/{id}")
    public ApiResponse<CardResponse> getById(@PathVariable @Min(1) Long id) {
        CardResponse card = cardService.getById(id);
        return ApiResponse.success(card, AppConstants.SUCCESS_RETRIEVE);
    }

    @PutMapping("/{id}")
    public ApiResponse<CardResponse> update(@PathVariable @Min(1) Long id, @Valid @RequestBody CardRequest input) {
        CardResponse updated = cardService.update(id, input);
        return ApiResponse.success(updated, AppConstants.SUCCESS_UPDATE);
    }

    @PatchMapping("/{id}")
    public ApiResponse<CardResponse> edit(@PathVariable @Min(1) Long id, @Valid @RequestBody CardPatchRequest input) {
        CardResponse edited = cardService.edit(id, input);
        return ApiResponse.success(edited, AppConstants.SUCCESS_UPDATE);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) Long id) {
        cardService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ApiResponse<PaginationResponse<CardResponse>> search(
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) @Min(1) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String direction) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<CardResponse> searchResult = cardService.search(cardNumber, studentName, pageable);
        return ApiResponse.success(PaginationResponse.from(searchResult), AppConstants.SUCCESS_FETCH_ALL);
    }
}
