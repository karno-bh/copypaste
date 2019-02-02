package org.copypaste.data;

/**
 * Common wrapper for all responses. Payload could be any type [but should have getters].
 * There are two factory methods for builders: {@link Response#good(java.lang.Object)} and {@link Response#bad()}.
 * The first one should preserve the compile type safety but the second one not, since on exception there is no real
 * value available.
 * @param <T> the type of the payload. In general should be specified as passed value with needed type.
 *
 * @author Sergey
 */
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

    /**
     * Builder for the good response. It is up to client to add exception if she wants.
     *
     * @param val - value of the good response
     * @param <K> - value type
     * @return builder for good response
     */
    public static <K> Builder<K> good(K val) {
        Builder<K> builder = new Builder<>();
        builder.success(true);
        builder.payload(val);
        return builder;
    }

    /**
     * Builder for the bad response. It is up to client to add exception if she wants.
     *
     * @return builder for non success response
     */
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
