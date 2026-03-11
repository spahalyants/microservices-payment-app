package com.iprody.paymentserviceapp.controller;

import com.iprody.paymentserviceapp.service.dto.NoteUpdateDto;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.service.PaymentService;
import com.iprody.paymentserviceapp.service.dto.CreatePaymentDto;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public PaymentDto create(@RequestBody CreatePaymentDto dto) {
        log.info("Creating payment with inquiryRefId: {}", dto.inquiryRefId());
        PaymentDto result = paymentService.create(dto);
        log.debug("Created payment: {}", result);
        return result;
    }

    @GetMapping("/{guid}")
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public PaymentDto get(@PathVariable UUID guid) {
        log.info("Fetching payment by guid: {}", guid);
        PaymentDto result = paymentService.findByGuid(guid);
        log.debug("Fetched payment: {}", result);
        return result;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public List<PaymentDto> findAll() {
        log.info("Fetching all payments");
        List<PaymentDto> result = paymentService.findAll();
        log.debug("Fetched {} payments", result.size());
        return result;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public Page<PaymentDto> search(
            @ModelAttribute PaymentFilter filter,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable
    ) {
        log.info("Searching payments with filter: {}", filter);
        Page<PaymentDto> result = paymentService.search(filter, pageable);
        log.debug("Search returned {} payments", result.getTotalElements());
        return result;
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto update(
            @PathVariable UUID guid,
            @RequestBody PaymentDto dto
    ) {
        log.info("Updating payment with guid: {}", guid);
        PaymentDto result = paymentService.update(guid, dto);
        log.debug("Updated payment: {}", result);
        return result;
    }

    @PatchMapping("/{guid}/note")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updateNote(
            @PathVariable UUID guid,
            @RequestBody NoteUpdateDto dto
    ) {
        log.info("Updating note for payment with guid: {}", guid);
        PaymentDto result = paymentService.updateNote(guid, dto.note());
        log.debug("Updated payment note: {}", result);
        return result;
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID guid) {
        log.info("Deleting payment with guid: {}", guid);
        paymentService.delete(guid);
        log.debug("Deleted payment with guid: {}", guid);
    }
}

/*

The `@ModelAttribute` annotation is key here —
it tells Spring to build a `PaymentFilter` object from **query parameters**.
So a request like `GET /payments/search?currency=USD&minAmount=10&maxAmount=100` automatically
populates the record's fields. Any parameter not provided stays `null`, which is exactly what
the factory checks for.

## The Flow Summary

HTTP GET /payments/search?currency=USD&minAmount=10&maxAmount=100

    → Controller: @ModelAttribute creates PaymentFilter(currency="USD", minAmount=10, maxAmount=100, null, null)
    → Service: passes filter to PaymentFilterFactory.fromFilter()
    → Factory: builds Specification = unrestricted AND hasCurrency("USD") AND amountBetween(10, 100)
    → Repository: executes findAll(spec) → generates SQL: SELECT * FROM payments WHERE currency='USD' AND amount BETWEEN 10 AND 100
    → Results flow back up as List<Payment>

The beauty of this design is its extensibility — if tomorrow you need to filter by status,
you just add a hasStatus() method in PaymentSpecifications, add a status field to PaymentFilter,
and add one more if block in the factory. No other code changes needed.

*/

/*

**Pagination** is handled by Spring's `Pageable` object,
automatically built from query parameters `page` and `size`.
`@PageableDefault` sets the defaults (page 0, 25 items).

**Sorting** is also part of `Pageable` — Spring reads the `sort` query parameter automatically.

Example requests:

# Status filter only
GET /payments/search?status=RECEIVED

# Status + currency + pagination
GET /payments/search?status=APPROVED&currency=USD&page=0&size=10

# Sorting by amount descending
GET /payments/search?sort=amount,desc

# Sorting by createdAt ascending (default direction)
GET /payments/search?sort=createdAt,asc

# Combined: filter + sort + pagination
GET /payments/search?currency=USD&status=PENDING&sort=amount,desc&page=0&size=25

*/
