package org.copypaste.data;

public class Response<T> {

    private final T payload;

    private final boolean success;

    private final String exception;

    public T getPayload() {
        return payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getException() {
        return exception;
    }

    private Response(T payload, boolean success, String exception) {
        this.payload = payload;
        this.success = success;
        this.exception = exception;
    }

    public static <K> Builder<K> good(K val) {
        Builder<K> builder = new Builder<>();
        builder.success(true);
        builder.payload(val);
        return builder;
    }

    public static Builder bad() {
        Builder builder = new Builder();
        builder.success(false);
        return builder;
    }

    public static class Builder<U> {

        private U payload;

        private boolean success;

        private String exception;

        public Builder<U> payload(U payload) {
            this.payload = payload;
            return this;
        }

        public Builder<U> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<U> exception(String exception) {
            this.exception = exception;
            return this;
        }

        public Response<U> build() {
            return new Response<U>(payload, success, exception);
        }
    }
}
