package com.nonameradio.app.core.domain.usecase;

public interface UseCase<T, R> {
    R execute(T params) throws Exception;
}

