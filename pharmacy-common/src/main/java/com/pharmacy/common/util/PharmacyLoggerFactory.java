package com.pharmacy.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PharmacyLoggerFactory {

    public static Logger getLogger() {
        return LoggerFactory.getLogger("PharmacyApp");
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
