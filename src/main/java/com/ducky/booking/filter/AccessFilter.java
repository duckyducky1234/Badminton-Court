package com.ducky.booking.filter;

import com.ducky.booking.controller.BaseServlet;
import com.ducky.booking.model.entity.Account;
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

@WebFilter({"/booking/*", "/ticket/*", "/admin/*"})
public class AccessFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute(BaseServlet.ACCOUNT_ATTRIBUTE);
        if (account == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;
        }
        String path = httpRequest.getServletPath();
        if ((path.startsWith("/admin/") || path.equals("/admin")) && !account.isAdmin()) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setAttribute(BaseServlet.ACCOUNT_ATTRIBUTE, account);
        chain.doFilter(request, response);
    }
}
