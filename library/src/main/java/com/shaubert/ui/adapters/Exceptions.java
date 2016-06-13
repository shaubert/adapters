package com.shaubert.ui.adapters;

public class Exceptions {

    private static Thread.UncaughtExceptionHandler exceptionHandler;

    public static void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        Exceptions.exceptionHandler = exceptionHandler;
    }

    public static void throwRuntime(RuntimeException ex) {
        if (exceptionHandler == null) {
            throw ex;
        }

        try {
            throw ex;
        } catch (Exception caughtEx) {
            exceptionHandler.uncaughtException(Thread.currentThread(), caughtEx);
        }
    }

}
