package com.iprody.paymentserviceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iprody.paymentserviceapp.AbstractPostgresIntegrationTest;
import com.iprody.paymentserviceapp.TestJwtFactory;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import com.iprody.paymentserviceapp.service.dto.CreatePaymentDto;
import com.iprody.paymentserviceapp.service.dto.NoteUpdateDto;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureMockMvc
class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String PAYMENTS_URL = "/payments";
    private static final UUID GUID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GUID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID GUID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID GUID_5 = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    // -----------------------------------------------------------------------
    // POST /payments
    // -----------------------------------------------------------------------

    @Test
    void shouldCreatePayment_whenAdminRole() throws Exception {
        CreatePaymentDto dto = new CreatePaymentDto(
                UUID.randomUUID(),
                new BigDecimal("123.45"),
                "EUR",
                PaymentStatus.PENDING,
                "Integration test payment"
        );

        String response = mockMvc.perform(post(PAYMENTS_URL)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guid").exists())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        UUID createdGuid = UUID.fromString(objectMapper.readTree(response).get("guid").asText());
        assertThat(paymentRepository.findById(createdGuid)).isPresent();
    }

    @Test
    void shouldReturn403_whenCreateWithReaderRole() throws Exception {
        CreatePaymentDto dto = new CreatePaymentDto(
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "USD",
                PaymentStatus.RECEIVED,
                "Should be forbidden"
        );

        mockMvc.perform(post(PAYMENTS_URL)
                        .with(TestJwtFactory.jwtWithRole("reader-user", "reader"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // GET /payments/{guid}
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnPayment_whenGetByGuidWithAdminRole() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL + "/" + GUID_2)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(GUID_2.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldReturnPayment_whenGetByGuidWithReaderRole() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL + "/" + GUID_1)
                        .with(TestJwtFactory.jwtWithRole("reader-user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(GUID_1.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void shouldReturn404_whenGetByNonExistentGuid() throws Exception {
        UUID nonExistent = UUID.randomUUID();

        mockMvc.perform(get(PAYMENTS_URL + "/" + nonExistent)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operation").value("GET " + PAYMENTS_URL + "/" + nonExistent))
                .andExpect(jsonPath("$.entityId").value(nonExistent.toString()));
    }

    // -----------------------------------------------------------------------
    // GET /payments
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnAllPayments_whenAdminRole() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.guid=='%s')]".formatted(GUID_1)).exists())
                .andExpect(jsonPath("$[?(@.guid=='%s')]".formatted(GUID_2)).exists())
                .andExpect(jsonPath("$[?(@.guid=='%s')]".formatted(GUID_3)).exists());
    }

    @Test
    void shouldReturnAllPayments_whenReaderRole() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL)
                        .with(TestJwtFactory.jwtWithRole("reader-user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -----------------------------------------------------------------------
    // GET /payments/search
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnFilteredPayments_whenSearchByCurrency() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL + "/search")
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .param("currency", "USD")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.currency=='USD')]").exists())
                .andExpect(jsonPath("$.content[?(@.currency=='EUR')]").doesNotExist());
    }

    @Test
    void shouldReturnFilteredPayments_whenSearchByStatus() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL + "/search")
                        .with(TestJwtFactory.jwtWithRole("reader-user", "reader"))
                        .param("status", "APPROVED")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.guid=='%s')]".formatted(GUID_2)).exists());
    }

    // -----------------------------------------------------------------------
    // PUT /payments/{guid}
    // -----------------------------------------------------------------------

    @Test
    void shouldUpdatePayment_whenAdminRole() throws Exception {
        PaymentDto dto = new PaymentDto();
        dto.setGuid(GUID_3);
        dto.setInquiryRefId(UUID.fromString("10000000-0000-0000-0000-000000000003"));
        dto.setAmount(new BigDecimal("99.99"));
        dto.setCurrency("CZK");
        dto.setStatus(PaymentStatus.APPROVED);
        dto.setNote("Updated note");

        mockMvc.perform(put(PAYMENTS_URL + "/" + GUID_3)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(GUID_3.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.note").value("Updated note"));
    }

    @Test
    void shouldReturn404_whenUpdateNonExistentPayment() throws Exception {
        UUID nonExistent = UUID.randomUUID();
        PaymentDto dto = new PaymentDto();
        dto.setAmount(new BigDecimal("10.00"));
        dto.setCurrency("USD");
        dto.setStatus(PaymentStatus.PENDING);

        mockMvc.perform(put(PAYMENTS_URL + "/" + nonExistent)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.entityId").value(nonExistent.toString()));
    }

    // -----------------------------------------------------------------------
    // PATCH /payments/{guid}/note
    // -----------------------------------------------------------------------

    @Test
    void shouldUpdateNote_whenAdminRole() throws Exception {
        NoteUpdateDto dto = new NoteUpdateDto("Patched note");

        mockMvc.perform(patch(PAYMENTS_URL + "/" + GUID_1 + "/note")
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(GUID_1.toString()))
                .andExpect(jsonPath("$.note").value("Patched note"));
    }

    @Test
    void shouldReturn404_whenUpdateNoteForNonExistentPayment() throws Exception {
        UUID nonExistent = UUID.randomUUID();
        NoteUpdateDto dto = new NoteUpdateDto("Some note");

        mockMvc.perform(patch(PAYMENTS_URL + "/" + nonExistent + "/note")
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.entityId").value(nonExistent.toString()));
    }

    // -----------------------------------------------------------------------
    // DELETE /payments/{guid}
    // -----------------------------------------------------------------------

    @Test
    void shouldDeletePayment_whenAdminRole() throws Exception {
        mockMvc.perform(delete(PAYMENTS_URL + "/" + GUID_5)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404_whenDeleteNonExistentPayment() throws Exception {
        UUID nonExistent = UUID.randomUUID();

        mockMvc.perform(delete(PAYMENTS_URL + "/" + nonExistent)
                        .with(TestJwtFactory.jwtWithRole("admin-user", "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.entityId").value(nonExistent.toString()));
    }

    @Test
    void shouldReturn403_whenDeleteWithReaderRole() throws Exception {
        mockMvc.perform(delete(PAYMENTS_URL + "/" + GUID_1)
                        .with(TestJwtFactory.jwtWithRole("reader-user", "reader")))
                .andExpect(status().isForbidden());
    }
}
