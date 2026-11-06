package ui.controller;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import ui.Navigation;

/**
 * Viser infosiden om BIThub og lar brukeren gå tilbake til siden infosiden ble åpnet fra,
 * innloggingen eller hjemmesiden.
 */
public final class AboutController {

    @FXML
    private Button backButton;

    private Navigation navigation;

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     * @throws NullPointerException hvis navigation er null
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Går tilbake til siden infosiden ble åpnet fra.
     */
    @FXML
    private void handleBack() {
        if (navigation == null) {
            throw new IllegalStateException("Navigasjonen er ikke konfigurert.");
        }
        navigation.returnFromAbout(backButton);
    }
}
