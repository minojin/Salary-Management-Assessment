package com.acme.salary.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaForwardingController.class)
class SpaForwardingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void clientSideRoutesServeTheAngularApp() throws Exception {
        mvc.perform(get("/insights")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/employees")).andExpect(forwardedUrl("/index.html"));
    }
}
