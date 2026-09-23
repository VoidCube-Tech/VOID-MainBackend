package com.voidcube.backend.core.utils;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public final class UuidUtils {
    
    private UuidUtils() {}

    public static UUID generateV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}