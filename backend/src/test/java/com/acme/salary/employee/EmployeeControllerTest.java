package com.acme.salary.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_BODY = """
            {"fullName":"Asha Rao","email":"asha@acme.com","jobTitle":"Software Engineer",
             "department":"Engineering","country":"IN","annualSalary":1800000,"hireDate":"2021-04-01"}
            """;

    private static final EmployeeResponse ASHA = new EmployeeResponse(1L, "Asha Rao", "asha@acme.com",
            "Software Engineer", "Engineering", Country.IN, "INR", 1_800_000L, LocalDate.of(2021, 4, 1),
            Instant.parse("2024-01-15T10:00:00Z"), Instant.parse("2024-06-01T09:30:00Z"));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EmployeeService service;

    @Test
    void listReturnsPageEnvelope() throws Exception {
        when(service.search(any(EmployeeSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ASHA), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.content[0].currency").value("INR"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2024-01-15T10:00:00Z"))
                .andExpect(jsonPath("$.content[0].updatedAt").value("2024-06-01T09:30:00Z"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void listBindsFiltersAndPaging() throws Exception {
        when(service.search(any(), any())).thenReturn(Page.empty());

        mvc.perform(get("/api/employees")
                        .param("search", "asha").param("country", "IN")
                        .param("department", "Engineering").param("jobTitle", "Software Engineer")
                        .param("page", "2").param("size", "50").param("sort", "annualSalary,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(eq(new EmployeeSearchCriteria("asha", Country.IN, "Engineering", "Software Engineer")),
                pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
        assertThat(pageable.getValue().getSort().getOrderFor("annualSalary").isDescending()).isTrue();
    }

    @Test
    void pageSizeIsCappedAt100() throws Exception {
        when(service.search(any(), any())).thenReturn(Page.empty());

        mvc.perform(get("/api/employees").param("size", "5000")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void unknownCountryFilterReturns400() throws Exception {
        mvc.perform(get("/api/employees").param("country", "XX")).andExpect(status().isBadRequest());
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(ASHA);

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createWithInvalidFieldsReturns400WithFieldErrors() throws Exception {
        String body = """
                {"fullName":"","email":"not-an-email","jobTitle":"SE","department":"Eng",
                 "country":"IN","annualSalary":-5,"hireDate":"2999-01-01"}
                """;

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.annualSalary").exists())
                .andExpect(jsonPath("$.errors.hireDate").exists());
    }

    @Test
    void createWithUnknownCountryReturns400() throws Exception {
        String body = VALID_BODY.replace("\"IN\"", "\"XX\"");

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(service.create(any())).thenThrow(new DuplicateEmailException("asha@acme.com"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void getUnknownEmployeeReturns404() throws Exception {
        when(service.get(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee 99 not found"));
    }

    @Test
    void updateReturnsUpdatedEmployee() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(ASHA);

        mvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("asha@acme.com"));
    }

    @Test
    void deleteReturns204AndUnknownReturns404() throws Exception {
        mvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());

        doThrow(new EmployeeNotFoundException(2L)).when(service).delete(2L);
        mvc.perform(delete("/api/employees/2")).andExpect(status().isNotFound());
    }
}
