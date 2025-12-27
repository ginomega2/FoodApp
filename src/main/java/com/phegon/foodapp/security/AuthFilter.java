package com.phegon.foodapp.security;

import ch.qos.logback.classic.pattern.SyslogStartConverter;
import com.phegon.foodapp.exceptions.CustomAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;




    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        System.out.println("ESTE ES EL TOKEN WEY      token---->>>>   "+token);
        System.out.println("esteb es elñ requets"+request.toString());
        System.out.println("***********************************************");
        if (token != null) {
            String email;

            try {

                email = jwtUtils.getUsernameFromToken(token);
                System.out.println(" ESTE ES EL EMAIL "+email);
            } catch (Exception ex) {
                BadCredentialsException authenticationException = new BadCredentialsException(ex.getMessage());
                customAuthenticationEntryPoint.commence(request, response, authenticationException);
                return;
            }
            System.out.println("ANTES DE USERDETAILS***********************************************");
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            System.out.println(" este es --  >>> userdetailos userDetails.getUsername()"+userDetails.getUsername());
            System.out.println(" este es --  >>> userdetailos userDetails.getAuthorities()"+userDetails.getAuthorities());
            System.out.println(" este es --  >>> userdetailos userDetails.getAuthorities())"+userDetails.getAuthorities());
            System.out.println("desues de detalles***********************************************");



            if (StringUtils.hasText(email)&& jwtUtils.isTokenValid(token,userDetails) ) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            }

        }

        try {
            filterChain.doFilter(request, response);

        }catch (Exception e) {
            log.error(e.getMessage());
        }


    }



    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenWithBearer = request.getHeader("Authorization");
        if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
            return tokenWithBearer.substring(7);
        }
        return null;
    }
}
