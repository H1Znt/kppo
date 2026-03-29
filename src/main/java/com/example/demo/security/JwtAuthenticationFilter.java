package com.example.demo.security;

import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
  @Autowired
  private JwtUtil jwtUtil;
  
  @Autowired
  private UserDetailsServiceImpl userDetailsService;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, 
                                 HttpServletResponse response, 
                                 FilterChain filterChain) 
          throws ServletException, IOException {
      
      String token = resolveToken(request);
      String username = null;
      
      if (token != null) {
          try {
              username = jwtUtil.getUsernameFromToken(token);
          } catch (Exception e) {
              logger.error("Cannot get username from token", e);
          }
      }
      
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);
          
          if (jwtUtil.validateToken(token, username)) {
              UsernamePasswordAuthenticationToken authToken = 
                  new UsernamePasswordAuthenticationToken(
                      userDetails, 
                      null, 
                      userDetails.getAuthorities()
                  );
              authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authToken);
          }
      }
      
      filterChain.doFilter(request, response);
  }
  
  private String resolveToken(HttpServletRequest request) {
      String bearer = request.getHeader("Authorization");
      if (bearer != null && bearer.startsWith("Bearer ")) {
          return bearer.substring(7).trim();
      }
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
          for (Cookie cookie : cookies) {
              if ("jwtToken".equals(cookie.getName())) {
                  return cookie.getValue();
              }
          }
      }
      return null;
  }
}
