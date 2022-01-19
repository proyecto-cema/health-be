package com.cema.health.config.filters;

import com.cema.health.domain.CemaUserDetails;
import com.cema.health.domain.User;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final RestTemplate restTemplate;
    private final String url;

    public JwtRequestFilter(RestTemplate restTemplate, @Value("${back-end.users.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url + "users";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(requestTokenHeader)) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(AUTHORIZATION_HEADER, requestTokenHeader);
            HttpEntity entity = new HttpEntity(httpHeaders);
            User user = restTemplate.postForObject(url, entity, User.class);

            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole());
                CemaUserDetails userDetails = new CemaUserDetails(user.getUserName(), "null",
                        Arrays.asList(grantedAuthority), user.getEstablishmentCuig(), requestTokenHeader);


                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}