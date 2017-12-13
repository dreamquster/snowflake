package org.storm.exceptions;

import java.io.IOException;

/**
 * Created by fm.chen on 2017/12/1.
 */
public class RpcCloseException extends IOException {
    public RpcCloseException() {
    }

    public RpcCloseException(String message) {
        super(message);
    }

    public RpcCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcCloseException(Throwable cause) {
        super(cause);
    }
}
