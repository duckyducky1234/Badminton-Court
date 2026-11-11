package com.ducky.booking.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@WebFilter("/*")
public class CsrfFilter implements Filter {
    private static final String TOKEN = "csrfToken";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String method = httpRequest.getMethod();
        if (("GET".equals(method) || "HEAD".equals(method)) && httpRequest.getServletPath().startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }
        httpResponse.setHeader("Cache-Control", "no-store");
        if ("POST".equals(method)) {
            HttpSession session = httpRequest.getSession(false);
            String expected = session == null ? null : (String) session.getAttribute(TOKEN);
            String submitted = httpRequest.getParameter(TOKEN);
            if (expected == null || submitted == null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else if ("GET".equals(method)) {
            HttpSession session = httpRequest.getSession();
            if (session.getAttribute(TOKEN) == null) {
                synchronized (session) {
                    if (session.getAttribute(TOKEN) == null) {
                        session.setAttribute(TOKEN, UUID.randomUUID().toString());
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }
}
