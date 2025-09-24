package com.nonameradio.app.core.architecture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
public class Result<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final Throwable error;

    public Result(@NonNull Status status, @Nullable T data, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> success(@NonNull T data) {
        return new Result<>(Status.SUCCESS, data, null);
    }

    public static <T> Result<T> error(@NonNull Throwable error) {
        return new Result<>(Status.ERROR, null, error);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    public enum Status {
        SUCCESS,
        ERROR
    }
}
