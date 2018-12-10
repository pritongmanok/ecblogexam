package com.epc.blog.persistence;

/**
 * The Result class encapsulates the result of running a service method.
 * If a service method run without errors, the result will be in Result.result
 * and a Result.status will have a value between 200 and 299.
 *
 * However, if there is an error running a service method, the Result.result
 * will typically be null, the Result.status will be an appropriate http status
 * code categorizing the error, and Result.errorMessage will describe the
 * reason for failure.
 * @param <T>
 */
public class Result<T> {

    private int status;
    private String errorMessage;
    private T result;

    public Result(T result, int status, String errorMessage) {
        this.result = result;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getResult() {
        return result;
    }
}
