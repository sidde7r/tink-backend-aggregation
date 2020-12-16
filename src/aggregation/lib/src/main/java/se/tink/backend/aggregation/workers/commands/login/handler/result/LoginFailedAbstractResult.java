package se.tink.backend.aggregation.workers.commands.login.handler.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class LoginFailedAbstractResult<T extends Exception> implements LoginResult {

    private T exception;
}
