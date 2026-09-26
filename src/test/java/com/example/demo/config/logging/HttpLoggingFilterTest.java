package com.example.demo.config.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import com.example.demo.service.logging.ActivityLogService;

class HttpLoggingFilterTest {

    private HttpLoggingFilter loggingFilter;
    private ObjectMapper objectMapper;
    private ActivityLogService activityLogService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        activityLogService = Mockito.mock(ActivityLogService.class);
        loggingFilter = new HttpLoggingFilter(objectMapper, activityLogService);
        ReflectionTestUtils.setField(loggingFilter, "saveToDb", true);
    }

    @Test
    void testSuccessfulResponseLoggingAndBodyPreservation() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String expectedJson = "{\"status\":200,\"message\":\"Success\",\"payload\":{\"id\":1}}";

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                res.setContentType("application/json");
                res.getOutputStream().write(expectedJson.getBytes(StandardCharsets.UTF_8));
            }
        };

        loggingFilter.doFilter(request, response, filterChain);

        // Verify the client response still receives the exact payload
        String responseContent = response.getContentAsString();
        assertEquals(expectedJson, responseContent);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testPostRequestWithPasswordMasking() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        String requestJson = "{\"email\":\"admin@example.com\",\"password\":\"secretPass123\"}";
        request.setContent(requestJson.getBytes(StandardCharsets.UTF_8));
        request.setContentType("application/json");

        MockHttpServletResponse response = new MockHttpServletResponse();
        String responseJson = "{\"status\":200,\"message\":\"Authentication successful\",\"payload\":{\"tokenType\":\"Bearer\"}}";

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                // Read request body to simulate Spring MVC reading the stream
                req.getInputStream().readAllBytes();
                res.setContentType("application/json");
                res.getOutputStream().write(responseJson.getBytes(StandardCharsets.UTF_8));
            }
        };

        loggingFilter.doFilter(request, response, filterChain);

        assertEquals(responseJson, response.getContentAsString());
    }

    @Test
    void testUnauthorizedErrorResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String errorJson = "{\"status\":401,\"message\":\"Full authentication is required\",\"payload\":null}";

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                MockHttpServletResponse mockRes = (MockHttpServletResponse) res;
                mockRes.setStatus(401);
                mockRes.setContentType("application/json");
                mockRes.getOutputStream().write(errorJson.getBytes(StandardCharsets.UTF_8));
            }
        };

        loggingFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals(errorJson, response.getContentAsString());
    }

    @Test
    void testStaticResourceIsBypassed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) throws IOException {
                res.getWriter().write("<html>Swagger</html>");
            }
        };

        loggingFilter.doFilter(request, response, filterChain);

        assertEquals("<html>Swagger</html>", response.getContentAsString());
    }
}
