package com.iprody.paymentserviceapp.controller;

import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<Payment> findAll() {
        return paymentService.findAll();
    }

    // http://localhost:8080/payments/search?currency=USD&minAmount=10&maxAmount=100

    @GetMapping("/search")
    public Page<Payment> search(
            @ModelAttribute PaymentFilter filter,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,           // Task 3
            @RequestParam(defaultValue = "25") int size            // Task 3
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentService.search(filter, pageable);
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