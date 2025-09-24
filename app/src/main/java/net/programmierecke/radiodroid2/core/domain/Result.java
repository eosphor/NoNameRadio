package net.programmierecke.radiodroid2.core.domain;

public abstract class Result<T> {

    public static final class Success<T> extends Result<T> {
        private final T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getValue() {
            return data;
        }

        @Override
        public Exception getError() {
            return null;
        }
    }

    public static final class Error<T> extends Result<T> {
        private final Exception exception;

        public Error(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public Exception getError() {
            return exception;
        }
    }

    public abstract boolean isSuccess();
    public abstract T getValue();
    public abstract Exception getError();

    public static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    public static <T> Result<T> error(Exception exception) {
        return new Error<>(exception);
    }
}

