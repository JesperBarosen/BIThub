package ui.controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import ui.client.AuthenticationClient;
import ui.client.AuthenticationResult;

/**
 * Falsk autentiseringstjeneste for tester av profil, navnebytte og passordbytte. Den husker hva
 * den ble kalt med, og kan kaste en valgt feil og kan holde igjen et kall til testen slipper det.
 */
public final class RecordingAuthenticationClient implements AuthenticationClient {

    final AtomicInteger changePasswordCalls = new AtomicInteger();
    final AtomicInteger logoutCalls = new AtomicInteger();
    final AtomicInteger changeNameCalls = new AtomicInteger();

    volatile String receivedToken; 
    volatile String receivedEmail;
    volatile String receivedOldPassword;
    volatile String receivedNewPassword;
    volatile String receivedName;

    volatile RuntimeException error;
    volatile CountDownLatch requestStarted;
    volatile CountDownLatch releaseRequest;

    @Override
    public AuthenticationResult login(String email, String password) {
        throw new UnsupportedOperationException("Innlogging testes i LoginControllerTest.");
    }

    @Override
    public AuthenticationResult register(String name, String email, String password) {
        throw new UnsupportedOperationException("Registrering testes i en egen kontroller.");
    }

    @Override
    public void changePassword(String token, String email, String oldPassword,
            String newPassword) {
        changePasswordCalls.incrementAndGet();
        receivedToken = token;
        receivedEmail = email;
        receivedOldPassword = oldPassword;
        receivedNewPassword = newPassword;
        waitAndMaybeFail();
    }

    /**
     * Husker kallet og svarer med navnet uten mellomrom rundt, slik serveren lagrer det.
     */
    @Override
    public String changeName(String token, String email, String name) {
        changeNameCalls.incrementAndGet();
        receivedToken = token;
        receivedEmail = email;
        receivedName = name;
        waitAndMaybeFail();
        return name.strip();
    }

    @Override
    public void logout(String token) {
        logoutCalls.incrementAndGet();
        receivedToken = token;
        waitAndMaybeFail();
    }

    /**
     * Gir beskjed om at kallet har startet, venter til testen slipper det og 
     * kaster eventuelt feilen testen har valgt.
     */
    private void waitAndMaybeFail() {
        CountDownLatch started = requestStarted;
        if (started != null) {
            started.countDown();
        }

        CountDownLatch release = releaseRequest;
        if (release != null) {
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Testforespørselen ble ikke frigitt.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Testforespørselen ble avbrutt.", e);
            }
        }

        RuntimeException failure = error;
        if (failure != null) {
            throw failure;
        }
    }
    
}