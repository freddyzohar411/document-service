package com.avensys.rts.documentservice.interceptor;

import com.avensys.rts.documentservice.util.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class JwtTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Bearer " + JwtUtil.getTokenFromContext());
    }
}
