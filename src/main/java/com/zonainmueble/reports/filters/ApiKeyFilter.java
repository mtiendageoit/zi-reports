package com.zonainmueble.reports.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@Component
public class ApiKeyFilter implements Filter {

  @Value("${app.api-key}")
  private String apiKey;

  @Value("${app.api-allowed-urls}")
  private String allowedUrls;

  private List<String> allowedUrlList;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    allowedUrlList = Arrays.asList(allowedUrls.split(","));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestApiKey = httpRequest.getParameter("api_key");

    if (!apiKey.equals(requestApiKey)) {
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      httpResponse.getWriter().write("Invalid API Key");
      return;
    }

    String referer = httpRequest.getHeader("Referer");

    System.out.println("referer: " + referer);
    // if (referer == null ||
    // allowedUrlList.stream().noneMatch(referer::startsWith)) {
    // httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    // httpResponse.getWriter().write("Access denied from this URL. Referer= " +
    // referer);
    // return;
    // }

    chain.doFilter(request, response);
  }

}
