package com.bgauction.gameservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceKeyFilter extends OncePerRequestFilter {

    private final String serviceInternalKey;

    public ServiceKeyFilter(@Value("${service.internal-key}") String serviceInternalKey) {
        this.serviceInternalKey = serviceInternalKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String serviceKey = request.getHeader("X-Service-Key");

        if (serviceKey == null || !serviceKey.equals(serviceInternalKey)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = "Missing or invalid required header: X-User-Id";
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
