package net.programmierecke.radiodroid2.core.domain.usecase;

public interface UseCase<T, R> {
    R execute(T params) throws Exception;
}

