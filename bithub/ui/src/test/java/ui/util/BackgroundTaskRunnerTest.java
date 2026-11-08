package ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Tester trådbruk, lastetilstand og avbrudd ved bakgrunnskall.
 */
class BackgroundTaskRunnerTest extends ApplicationTest {

    private BackgroundTaskRunner runner;

    @Override
    public void start(Stage stage) {
        runner = new BackgroundTaskRunner();
    }

    @AfterEach
    void tearDown() {
        interact(() -> runner.cancel());
    }

    @Test
    void rejectsControlFromOutsideFxThread() {
        AtomicBoolean operationStarted = new AtomicBoolean();
        assertFalse(Platform.isFxApplicationThread());

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runner.execute(() -> {
                    operationStarted.set(true);
                    return "Ferdig";
                }, value -> {}, failure -> {}));
        assertEquals("Bakgrunnskall må styres fra JavaFX-tråden.", error.getMessage());
        assertThrows(IllegalStateException.class, runner::cancel);
        assertFalse(operationStarted.get());
        interact(() -> assertFalse(runner.runningProperty().get()));
    }

    @Test
    void runsOperationInBackgroundAndHandlesSuccessOnFxThread() throws Exception {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        AtomicBoolean callbackOnFxThread = new AtomicBoolean();
        AtomicBoolean runningInCallback = new AtomicBoolean(true);

        interact(() -> runner.execute(() -> Platform.isFxApplicationThread(), value -> {
            callbackOnFxThread.set(Platform.isFxApplicationThread());
            runningInCallback.set(runner.runningProperty().get());
            result.complete(value);
        }, error -> result.completeExceptionally(error)));

        assertFalse(result.get(5, TimeUnit.SECONDS));
        assertTrue(callbackOnFxThread.get());
        assertFalse(runningInCallback.get());
    }

    @Test
    void handlesFailureOnFxThreadAndClearsLoadingState() throws Exception {
        IOException expected = new IOException("Serveren er utilgjengelig.");
        CompletableFuture<Throwable> failure = new CompletableFuture<>();
        AtomicBoolean callbackOnFxThread = new AtomicBoolean();
        AtomicBoolean runningInCallback = new AtomicBoolean(true);

        interact(() -> runner.execute(() -> {
            throw expected;
        }, value -> failure.completeExceptionally(new AssertionError("Oppgaven skulle ha feilet.")),
                error -> {
                    callbackOnFxThread.set(Platform.isFxApplicationThread());
                    runningInCallback.set(runner.runningProperty().get());
                    failure.complete(error);
                }));

        assertSame(expected, failure.get(5, TimeUnit.SECONDS));
        assertTrue(callbackOnFxThread.get());
        assertFalse(runningInCallback.get());
    }

    @Test
    void keepsLoadingStateAndIgnoresDuplicateCall() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean duplicateStarted = new AtomicBoolean();
        CompletableFuture<String> result = new CompletableFuture<>();

        try {
            interact(() -> runner.execute(() -> {
                started.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Testoppgaven ble ikke frigitt.");
                }
                return "Ferdig";
            }, value -> result.complete(value), error -> result.completeExceptionally(error)));

            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                assertTrue(runner.runningProperty().get());

                runner.execute(() -> {
                    duplicateStarted.set(true);
                    return "Ekstra kall";
                }, value -> result.complete(value), error -> result.completeExceptionally(error));

                assertTrue(runner.runningProperty().get());
            });

            release.countDown();

            assertEquals("Ferdig", result.get(5, TimeUnit.SECONDS));
            interact(() -> assertFalse(runner.runningProperty().get()));
            assertFalse(duplicateStarted.get());
        } finally {
            release.countDown();
        }
    }

    @Test
    void ignoresLateResultAfterCancellationAndAllowsNewCall() throws Exception {
        CompletableFuture<Void> releaseOld = new CompletableFuture<>();
        CompletableFuture<Void> releaseNew = new CompletableFuture<>();
        CountDownLatch oldStarted = new CountDownLatch(1);
        AtomicReference<Thread> oldWorker = new AtomicReference<>();
        AtomicBoolean oldCallbackCalled = new AtomicBoolean();
        CompletableFuture<String> newResult = new CompletableFuture<>();

        try {
            interact(() -> runner.execute(() -> {
                oldWorker.set(Thread.currentThread());
                oldStarted.countDown();

                // Simulerer arbeid som fortsetter selv om tråden avbrytes.
                releaseOld.join();
                return "Gammelt svar";
            }, value -> oldCallbackCalled.set(true), error -> oldCallbackCalled.set(true)));

            assertTrue(oldStarted.await(5, TimeUnit.SECONDS));

            interact(() -> {
                runner.cancel();
                assertFalse(runner.runningProperty().get());

                runner.execute(() -> {
                    releaseNew.join();
                    return "Nytt svar";
                }, value -> newResult.complete(value),
                        error -> newResult.completeExceptionally(error));

                assertTrue(runner.runningProperty().get());
            });

            releaseOld.complete(null);

            // Venter på at den gamle oppgaven faktisk har avsluttet.
            Thread worker = oldWorker.get();
            worker.join(5000);
            assertFalse(worker.isAlive());

            interact(() -> {
                assertFalse(oldCallbackCalled.get());
                assertTrue(runner.runningProperty().get());
            });

            releaseNew.complete(null);

            assertEquals("Nytt svar", newResult.get(5, TimeUnit.SECONDS));
            interact(() -> assertFalse(runner.runningProperty().get()));
            assertFalse(oldCallbackCalled.get());
        } finally {
            releaseOld.complete(null);
            releaseNew.complete(null);
        }
    }
}
