package ui.util;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Task;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Kjører ett bakgrunnskall om gangen for en kontroller.
 *
 * <p>Oppgaven kjøres på en bakgrunnstråd, mens resultat, feil og lastetilstand håndteres på
 * JavaFX-tråden. Kontrolleren må avbryte kallet når skjermen forlates.
 */
public final class BackgroundTaskRunner {

    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(false);
    private Task<?> activeTask;

    /**
     * Gir lastetilstanden som kontrolleren kan binde UI-elementer til.
     *
     * @return en skrivebeskyttet egenskap som er sann mens et kall pågår
     */
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    /**
     * Starter et bakgrunnskall hvis ingen oppgave allerede pågår.
     *
     * <p>Inndata fra UI må leses før denne metoden kalles. Selve oppgaven skal ikke lese eller
     * endre JavaFX-elementer. Begge tilbakemeldingene kjøres på JavaFX-tråden.
     *
     * @param <ResulT>  typen resultat som oppgaven returnerer
     * @param operation arbeidet som skal utføres i bakgrunnen
     * @param onSuccess handlingen ved vellykket gjennomføring
     * @param onFailure handlingen ved feil
     * @throws NullPointerException  hvis en av parameterne er null
     * @throws IllegalStateException hvis metoden kalles utenfor JavaFX-tråden
     */
    public <ResulT> void execute(Callable<ResulT> operation, Consumer<ResulT> onSuccess,
            Consumer<Throwable> onFailure) {
        requireFxThread();
        Objects.requireNonNull(operation);
        Objects.requireNonNull(onSuccess);
        Objects.requireNonNull(onFailure);

        if (activeTask != null) {
            return;
        }

        Task<ResulT> task = new Task<>() {
            @Override
            protected ResulT call() throws Exception {
                return operation.call();
            }
        };

        task.setOnSucceeded(event -> {
            if (finish(task)) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(event -> {
            if (finish(task)) {
                onFailure.accept(task.getException());
            }
        });

        Thread worker = new Thread(task, "bithub-background-task");
        worker.setDaemon(true);

        activeTask = task;
        running.set(true);
        worker.start();
    }

    /**
     * Avbryter det aktive kallet og ignorerer senere resultat eller feil.
     *
     * <p>Avbrudd garanterer ikke at serveren stopper en påbegynt lagring. Metoden hindrer at svaret
     * oppdaterer en skjerm brukeren har forlatt.
     *
     * @throws IllegalStateException hvis metoden kalles utenfor JavaFX-tråden
     */
    public void cancel() {
        requireFxThread();
        Task<?> task = activeTask;
        activeTask = null;

        if (task != null) {
            task.cancel(true);
        }

        running.set(false);
    }

    /**
     * Avslutter bare oppgaven som fortsatt tilhører denne skjermen.
     */
    private boolean finish(Task<?> task) {
        if (activeTask != task) {
            return false;
        }

        activeTask = null;
        running.set(false);
        return true;
    }

    private static void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Bakgrunnskall må styres fra JavaFX-tråden.");
        }
    }
}
