package com.nttdata.fhuichic.controller;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.net.InetAddress;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

    private static final String START_TIME = "start-time";
    private static final String X_DURATION_HEADER = "x-duration";
    private static final String X_HOST_HEADER = "x-host";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        long startTime = (Long) requestContext.getProperty(START_TIME);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        responseContext.getHeaders().add(X_DURATION_HEADER, duration + "ms");
        responseContext.getHeaders().add(X_HOST_HEADER, InetAddress.getLocalHost().getHostAddress());
    }
}
