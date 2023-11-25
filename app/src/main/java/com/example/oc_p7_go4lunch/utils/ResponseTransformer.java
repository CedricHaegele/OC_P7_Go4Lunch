package com.example.oc_p7_go4lunch.utils;

public interface ResponseTransformer<T, R> {
    R apply(T input);
}
