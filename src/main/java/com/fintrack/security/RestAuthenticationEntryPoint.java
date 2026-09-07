package com.fintrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security'nin varsayılan davranışı: özel bir {@code AuthenticationEntryPoint}
 * tanımlanmazsa (ve {@code httpBasic()}/form login de yoksa) kimliksiz istekler
 * 401 değil <b>403</b> ile reddedilir ({@code Http403ForbiddenEntryPoint}).
 * REST API'de bu yanıltıcıdır: 401 "kimliğini doğrula", 403 "kimliğin belli
 * ama bu kaynağa yetkin yok" anlamına gelmeli. Bu sınıf o ayrımı düzeltir ve
 * {@link com.fintrack.common.exception.GlobalExceptionHandler} ile aynı
 * {@link ErrorResponse} formatını üretir (filter seviyesinde olduğu için
 * controller advice buraya erişemiyor).
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Kimlik doğrulama gerekli",
                request.getRequestURI()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
