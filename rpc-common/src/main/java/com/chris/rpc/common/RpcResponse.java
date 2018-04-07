package com.chris.rpc.common;

/**
 * 封装RPC响应
 * 封装响应的Object
 * RpcResponse
 * author: Chris
 * time：2018.03.05 11:46
 */
public class RpcResponse {

    private String requestId;
    private Throwable error;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public boolean isError() {
        return error != null;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
