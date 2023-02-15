/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wrapper of a given {@link CompletableFuture} to add some
 * {@link CompletableFuture} which need to be canceled when the wrapped
 * {@link CompletableFuture} is cancelled.
 * 
 * @author Angelo ZERR
 *
 * @param <T>
 */
class CompletableFutureWrapper<T> extends CompletableFuture<T> implements ExtendedCancelChecker {

	private final CompletableFuture<T> delegate;
	private List<CompletableFuture<?>> futuresToCancel;

	public CompletableFutureWrapper(CompletableFuture<T> delegate) {
		this.delegate = delegate;
		this.futuresToCancel = new ArrayList<>();
	}

	@Override
	public void checkCanceled() {
		if (delegate.isCancelled())
			throw new CancellationException();
	}

	@Override
	public <U> CompletableFuture<U> cancelIfNeeded(CompletableFuture<U> futureToCancel) {
		this.futuresToCancel.add(futureToCancel);
		return futureToCancel;
	}

	/**
	 * Cancel the delegate future and the list of future to cancel.
	 */
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// Cancel the root completable future
		boolean result = delegate.cancel(mayInterruptIfRunning);
		// Cancel all futures to cancel
		for (CompletableFuture<?> futureToCancel : futuresToCancel) {
			futureToCancel.cancel(mayInterruptIfRunning);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public boolean isDone() {
		return delegate.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return delegate.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return delegate.get(timeout, unit);
	}

	@Override
	public T join() {
		return delegate.join();
	}

	@Override
	public T getNow(T valueIfAbsent) {
		return delegate.getNow(valueIfAbsent);
	}

	@Override
	public boolean complete(T value) {
		return delegate.complete(value);
	}

	@Override
	public boolean completeExceptionally(Throwable ex) {
		return delegate.completeExceptionally(ex);
	}

	@Override
	public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
		return delegate.thenApply(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
		return delegate.thenApplyAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
		return delegate.thenApplyAsync(fn, executor);
	}

	@Override
	public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
		return delegate.thenAccept(action);
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
		return delegate.thenAcceptAsync(action);
	}

	@Override
	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
		return delegate.thenAcceptAsync(action, executor);
	}

	@Override
	public CompletableFuture<Void> thenRun(Runnable action) {
		return delegate.thenRun(action);
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action) {
		return delegate.thenRunAsync(action);
	}

	@Override
	public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
		return delegate.thenRunAsync(action, executor);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return delegate.thenCombine(other, fn);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return delegate.thenCombineAsync(other, fn);
	}

	@Override
	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
		return delegate.thenCombineAsync(other, fn, executor);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return delegate.thenAcceptBoth(other, action);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return delegate.thenAcceptBothAsync(other, action);
	}

	@Override
	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action, Executor executor) {
		return delegate.thenAcceptBothAsync(other, action, executor);
	}

	@Override
	public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		return delegate.runAfterBoth(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		return delegate.runAfterBothAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		return delegate.runAfterBothAsync(other, action, executor);
	}

	@Override
	public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
		return delegate.applyToEither(other, fn);
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
		return delegate.applyToEitherAsync(other, fn);
	}

	@Override
	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn,
			Executor executor) {
		return delegate.applyToEitherAsync(other, fn, executor);
	}

	@Override
	public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
		return delegate.acceptEither(other, action);
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
		return delegate.acceptEitherAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action,
			Executor executor) {
		return delegate.acceptEitherAsync(other, action, executor);
	}

	@Override
	public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
		return delegate.runAfterEither(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
		return delegate.runAfterEitherAsync(other, action);
	}

	@Override
	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		return delegate.runAfterEitherAsync(other, action, executor);
	}

	@Override
	public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		return delegate.thenCompose(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
		return delegate.thenComposeAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
			Executor executor) {
		return delegate.thenComposeAsync(fn, executor);
	}

	@Override
	public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
		return delegate.whenComplete(action);
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
		return delegate.whenCompleteAsync(action);
	}

	@Override
	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
		return delegate.whenCompleteAsync(action, executor);
	}

	@Override
	public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		return delegate.handle(fn);
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
		return delegate.handleAsync(fn);
	}

	@Override
	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
		return delegate.handleAsync(fn, executor);
	}

	@Override
	public CompletableFuture<T> toCompletableFuture() {
		return delegate.toCompletableFuture();
	}

	@Override
	public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
		return delegate.exceptionally(fn);
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public boolean isCompletedExceptionally() {
		return delegate.isCompletedExceptionally();
	}

	@Override
	public void obtrudeValue(T value) {
		delegate.obtrudeValue(value);
	}

	@Override
	public void obtrudeException(Throwable ex) {
		delegate.obtrudeException(ex);
	}

	@Override
	public int getNumberOfDependents() {
		return delegate.getNumberOfDependents();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

}
