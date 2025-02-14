/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.core.impl.utils.futures;

import static androidx.core.util.Preconditions.checkNotNull;

import androidx.arch.core.util.Function;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.util.Preconditions;

import com.google.common.util.concurrent.ListenableFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for generating specific implementations of {@link ListenableFuture}.
 */
public final class Futures {

    /**
     * Returns an implementation of {@link ListenableFuture} which immediately contains a result.
     *
     * @param value The result that is immediately set on the future.
     * @param <V>   The type of the result.
     * @return A future which immediately contains the result.
     */
    public static <V> @NonNull ListenableFuture<V> immediateFuture(@Nullable V value) {
        if (value == null) {
            return ImmediateFuture.nullFuture();
        }

        return new ImmediateFuture.ImmediateSuccessfulFuture<>(value);
    }

    /**
     * Returns an implementation of {@link ListenableFuture} which immediately contains an
     * exception that will be thrown by {@link Future#get()}.
     *
     * @param cause The cause of the {@link ExecutionException} that will be thrown by
     * {@link Future#get()}.
     * @param <V>   The type of the result.
     * @return A future which immediately contains an exception.
     */
    public static <V> @NonNull ListenableFuture<V> immediateFailedFuture(@NonNull Throwable cause) {
        return new ImmediateFuture.ImmediateFailedFuture<>(cause);
    }

    /**
     * Returns an implementation of {@link ScheduledFuture} which immediately contains an
     * exception that will be thrown by {@link Future#get()}.
     *
     * @param cause The cause of the {@link ExecutionException} that will be thrown by
     * {@link Future#get()}.
     * @param <V>   The type of the result.
     * @return A future which immediately contains an exception.
     */
    public static <V> @NonNull ScheduledFuture<V> immediateFailedScheduledFuture(
            @NonNull Throwable cause) {
        return new ImmediateFuture.ImmediateFailedScheduledFuture<>(cause);
    }

    /**
     * Returns a new {@code Future} whose result is asynchronously derived from the result
     * of the given {@code Future}. If the given {@code Future} fails, the returned {@code Future}
     * fails with the same exception (and the function is not invoked).
     *
     * @param input    The future to transform
     * @param function A function to transform the result of the input future to the result of the
     *                 output future
     * @param executor Executor to run the function in.
     * @return A future that holds result of the function (if the input succeeded) or the original
     * input's failure (if not)
     */
    public static <I, O> @NonNull ListenableFuture<O> transformAsync(
            @NonNull ListenableFuture<I> input,
            @NonNull AsyncFunction<? super I, ? extends O> function,
            @NonNull Executor executor) {
        ChainingListenableFuture<I, O> output = new ChainingListenableFuture<I, O>(function, input);
        input.addListener(output, executor);
        return output;
    }

    /**
     * Returns a new {@code Future} whose result is derived from the result of the given {@code
     * Future}. If {@code input} fails, the returned {@code Future} fails with the same
     * exception (and the function is not invoked)
     *
     * @param input    The future to transform
     * @param function A function to transform the results of the provided future to the results of
     *                 the returned future.
     * @param executor Executor to run the function in.
     * @return A future that holds result of the transformation.
     */
    public static <I, O> @NonNull ListenableFuture<O> transform(
            @NonNull ListenableFuture<I> input,
            @NonNull Function<? super I, ? extends O> function,
            @NonNull Executor executor) {
        checkNotNull(function);
        return transformAsync(input, new AsyncFunction<I, O>() {

            @Override
            public @NonNull ListenableFuture<O> apply(I input) {
                return immediateFuture(function.apply(input));
            }
        }, executor);
    }

    private static final Function<?, ?> IDENTITY_FUNCTION = new Function<Object, Object>() {
        @Override
        public Object apply(Object input) {
            return input;
        }
    };

    /**
     * Propagates the result of the given {@code ListenableFuture} to the given {@link
     * CallbackToFutureAdapter.Completer} directly.
     *
     * <p>If {@code input} fails, the failure will be propagated to the {@code completer}.
     *
     * @param input     The future being propagated.
     * @param completer The completer which will receive the result of the provided future.
     */
    @SuppressWarnings("LambdaLast") // ListenableFuture not needed for SAM conversion
    public static <V> void propagate(
            @NonNull ListenableFuture<V> input,
            final CallbackToFutureAdapter.@NonNull Completer<V> completer) {
        @SuppressWarnings({"unchecked"}) // Input of function is same as output
                Function<? super V, ? extends V> identityTransform =
                (Function<? super V, ? extends V>) IDENTITY_FUNCTION;
        // Use direct executor here since function is just unpacking the output and should be quick
        propagateTransform(input, identityTransform, completer, CameraXExecutors.directExecutor());
    }

    /**
     * Propagates the result of the given {@code ListenableFuture} to the given {@link
     * CallbackToFutureAdapter.Completer} by applying the provided transformation function.
     *
     * <p>If {@code input} fails, the failure will be propagated to the {@code completer} (and the
     * function is not invoked)
     *
     * @param input     The future to transform.
     * @param function  A function to transform the results of the provided future to the results of
     *                  the provided completer.
     * @param completer The completer which will receive the result of the provided future.
     * @param executor  Executor to run the function in.
     */
    public static <I, O> void propagateTransform(
            final @NonNull ListenableFuture<I> input,
            final @NonNull Function<? super I, ? extends O> function,
            final CallbackToFutureAdapter.@NonNull Completer<O> completer,
            @NonNull Executor executor) {
        propagateTransform(true, input, function, completer, executor);
    }

    /**
     * Propagates the result of the given {@code ListenableFuture} to the given {@link
     * CallbackToFutureAdapter.Completer} by applying the provided transformation function.
     *
     * <p>If {@code input} fails, the failure will be propagated to the {@code completer} (and the
     * function is not invoked)
     *
     * @param propagateCancellation {@code true} to propagate the cancellation from completer to
     *                              input future.
     * @param input                 The future to transform.
     * @param function              A function to transform the results of the provided future to
     *                              the results of the provided completer.
     * @param completer             The completer which will receive the result of the provided
     *                              future.
     * @param executor              Executor to run the function in.
     */
    private static <I, O> void propagateTransform(
            boolean propagateCancellation,
            final @NonNull ListenableFuture<I> input,
            final @NonNull Function<? super I, ? extends O> function,
            final CallbackToFutureAdapter.@NonNull Completer<O> completer,
            @NonNull Executor executor) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(function);
        Preconditions.checkNotNull(completer);
        Preconditions.checkNotNull(executor);

        addCallback(input, new FutureCallback<I>() {
            @Override
            public void onSuccess(@Nullable I result) {
                try {
                    completer.set(function.apply(result));
                } catch (Throwable t) {
                    completer.setException(t);
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                completer.setException(t);
            }
        }, executor);

        if (propagateCancellation) {
            // Propagate cancellation from completer to input future
            completer.addCancellationListener(new Runnable() {
                @Override
                public void run() {
                    input.cancel(true);
                }
            }, CameraXExecutors.directExecutor());
        }
    }

    /**
     * Returns a {@code ListenableFuture} whose result is set from the supplied future when it
     * completes.
     *
     * <p>Cancelling the supplied future will also cancel the returned future, but
     * cancelling the returned future will have no effect on the supplied future.
     */
    public static <V> @NonNull ListenableFuture<V> nonCancellationPropagating(
            @NonNull ListenableFuture<V> future) {
        Preconditions.checkNotNull(future);

        if (future.isDone()) {
            return future;
        }

        ListenableFuture<V> output = CallbackToFutureAdapter.getFuture(
                completer -> {
                    @SuppressWarnings({"unchecked"}) // Input of function is same as output
                            Function<? super V, ? extends V> identityTransform =
                            (Function<? super V, ? extends V>) IDENTITY_FUNCTION;
                    propagateTransform(false, future, identityTransform, completer,
                            CameraXExecutors.directExecutor());
                    return "nonCancellationPropagating[" + future + "]";
                });
        return output;
    }

    /**
     * Creates a new {@code ListenableFuture} whose value is a list containing the values of all its
     * successful input futures. The list of results is in the same order as the input list, and if
     * any of the provided futures fails or is canceled, its corresponding position will contain
     * {@code null} (which is indistinguishable from the future having a successful value of {@code
     * null}).
     *
     * <p>Canceling this future will attempt to cancel all the component futures.
     *
     * @param futures futures to combine
     * @return a future that provides a list of the results of the component futures
     */
    public static <V> @NonNull ListenableFuture<List<V>> successfulAsList(
            @NonNull Collection<? extends ListenableFuture<? extends V>> futures) {
        return new ListFuture<V>(new ArrayList<>(futures), false,
                CameraXExecutors.directExecutor());
    }

    /**
     * Creates a new {@code ListenableFuture} whose value is a list containing the values of all its
     * input futures, if all succeed.
     *
     * <p>The list of results is in the same order as the input list.
     *
     * <p>Canceling this future will attempt to cancel all the component futures, and if any of the
     * provided futures fails or is canceled, this one is, too.
     *
     * @param futures futures to combine
     * @return a future that provides a list of the results of the component futures
     */
    public static <V> @NonNull ListenableFuture<List<V>> allAsList(
            @NonNull Collection<? extends ListenableFuture<? extends V>> futures) {
        return new ListFuture<V>(new ArrayList<>(futures), true, CameraXExecutors.directExecutor());
    }

    /**
     * Registers separate success and failure callbacks to be run when the {@code Future}'s
     * computation is {@linkplain java.util.concurrent.Future#isDone() complete} or, if the
     * computation is already complete, immediately.
     *
     * @param future   The future attach the callback to.
     * @param callback The callback to invoke when {@code future} is completed.
     * @param executor The executor to run {@code callback} when the future completes.
     */
    public static <V> void addCallback(
            final @NonNull ListenableFuture<V> future,
            final @NonNull FutureCallback<? super V> callback,
            @NonNull Executor executor) {
        Preconditions.checkNotNull(callback);
        future.addListener(new CallbackListener<V>(future, callback), executor);
    }

    /**
     * See {@link #addCallback(ListenableFuture, FutureCallback, Executor)} for behavioral notes.
     */
    private static final class CallbackListener<V> implements Runnable {
        final Future<V> mFuture;
        final FutureCallback<? super V> mCallback;

        CallbackListener(Future<V> future, FutureCallback<? super V> callback) {
            mFuture = future;
            mCallback = callback;
        }

        @Override
        public void run() {
            final V value;
            try {
                value = getDone(mFuture);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    mCallback.onFailure(e);
                } else {
                    mCallback.onFailure(cause);
                }
                return;
            } catch (RuntimeException | Error e) {
                mCallback.onFailure(e);
                return;
            }
            mCallback.onSuccess(value);
        }

        @Override
        public @NonNull String toString() {
            return getClass().getSimpleName() + "," + mCallback;
        }
    }

    /**
     * Returns the result of the input {@code Future}, which must have already completed.
     *
     * <p>The benefits of this method are twofold. First, the name "getDone" suggests to readers
     * that the {@code Future} is already done. Second, if buggy code calls {@code getDone} on a
     * {@code Future} that is still pending, the program will throw instead of block.
     *
     * @throws ExecutionException    if the {@code Future} failed with an exception
     * @throws CancellationException if the {@code Future} was cancelled
     * @throws IllegalStateException if the {@code Future} is not done
     */
    public static <V> @Nullable V getDone(@NonNull Future<V> future) throws ExecutionException {
        /*
         * We throw IllegalStateException, since the call could succeed later. Perhaps we
         * "should" throw IllegalArgumentException, since the call could succeed with a different
         * argument. Those exceptions' docs suggest that either is acceptable. Google's Java
         * Practices page recommends IllegalArgumentException here, in part to keep its
         * recommendation simple: Static methods should throw IllegalStateException only when
         * they use static state.
         *
         * Why do we deviate here? The answer: We want for fluentFuture.getDone() to throw the same
         * exception as Futures.getDone(fluentFuture).
         */
        Preconditions.checkState(future.isDone(), "Future was expected to be done, " + future);
        return getUninterruptibly(future);
    }

    /**
     * Invokes {@code Future.}{@link Future#get() get()} uninterruptibly.
     *
     * @throws ExecutionException    if the computation threw an exception
     * @throws CancellationException if the computation was cancelled
     */
    public static <V> @Nullable V getUninterruptibly(@NonNull Future<V> future)
            throws ExecutionException {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Returns a future that delegates to the supplied future but will finish early
     * (via a TimeoutException) if the specified duration expires.
     *
     * <p> The input future itself is not canceled at timeout and thus keeps continuing until it
     * is completed (if ever). See
     * {@link #makeTimeoutFuture(long, ScheduledExecutorService, Object, boolean, ListenableFuture)}
     * if you need this behavior.
     *
     * @param timeoutMillis     When to time out the future in milliseconds.
     * @param scheduledExecutor The executor service to enforce the timeout.
     * @param input             The future to delegate to.
     */
    public static <V> @NonNull ListenableFuture<V> makeTimeoutFuture(
            long timeoutMillis,
            @NonNull ScheduledExecutorService scheduledExecutor,
            @NonNull ListenableFuture<V> input) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            propagate(input, completer);
            if (!input.isDone()) {
                ScheduledFuture<?> timeoutFuture = scheduledExecutor.schedule(
                        () -> completer.setException(new TimeoutException("Future[" + input + "] "
                                + "is not done within " + timeoutMillis + " ms.")),
                        timeoutMillis, TimeUnit.MILLISECONDS);
                input.addListener(
                        () -> timeoutFuture.cancel(true), CameraXExecutors.directExecutor());
            }
            return "TimeoutFuture[" + input + "]";
        });
    }

    /**
     * Returns a future that delegates to the supplied future but will finish early normally with
     * the provided default value if the specified duration expires.
     *
     * @param timeoutMillis        When to time out the future in milliseconds.
     * @param scheduledExecutor    The executor service to enforce the timeout.
     * @param defaultValue         The default value to complete output future with in case of
     *                             timeout.
     * @param cancelInputAtTimeout If true, the input future will be canceled at timeout.
     * @param input                The future to delegate to.
     */
    public static <V> @NonNull ListenableFuture<V> makeTimeoutFuture(
            long timeoutMillis,
            @NonNull ScheduledExecutorService scheduledExecutor,
            @Nullable V defaultValue,
            boolean cancelInputAtTimeout,
            @NonNull ListenableFuture<V> input) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            propagate(input, completer);
            if (!input.isDone()) {
                ScheduledFuture<?> timeoutFuture = scheduledExecutor.schedule(
                        () -> {
                            completer.set(defaultValue);
                            if (cancelInputAtTimeout) {
                                input.cancel(true);
                            }
                        },
                        timeoutMillis, TimeUnit.MILLISECONDS);
                input.addListener(
                        () -> timeoutFuture.cancel(true), CameraXExecutors.directExecutor());
            }
            return "TimeoutFuture[" + input + "]";
        });
    }

    /**
     * Creates a {@link ListenableFuture} signaling the completion of the {@code input} future,
     * regardless of whether it completes successfully or with an exception. This is useful for
     * monitoring when the {@code input} future is done without needing to handle its result.
     *
     * @param input The input ListenableFuture to monitor.
     * @param <V> The type of the result within the input future (not used in the returned future).
     * @return A ListenableFuture that completes when the {@code input} future completes.
     */
    public static <V> @NonNull ListenableFuture<Void> transformAsyncOnCompletion(
            @NonNull ListenableFuture<V> input) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            input.addListener(() -> completer.set(null), CameraXExecutors.directExecutor());
            return "transformVoidFuture [" + input + "]";
        });
    }

    /**
     * Should not be instantiated.
     */
    private Futures() {}
}
