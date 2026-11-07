package ui.util;

import java.util.Objects;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Lar brukeren vise passordet i et passordfelt.
 *
 * <p>{@link PasswordField} kan ikke vise teksten selv. Derfor legges et vanlig {@link TextField} på
 * samme plass, og de to feltene deler tekst. En avkrysningsboks bestemmer hvilket av feltene som
 * vises. Passordet er skjult som standard.
 */
public final class PasswordVisibility {

    private PasswordVisibility() {
    }

    /**
     * Kobler et passordfelt til et tekstfelt som viser passordet når avkrysningsboksen er krysset
     * av.
     *
     * <p>Tekstfeltet får samme tekst, deaktivering og handling ved Enter som passordfeltet, slik at
     * kontrolleren bare trenger å lese og tømme passordfeltet.
     *
     * @param showPassword avkrysningsboksen som viser passordet
     * @param hiddenField  passordfeltet som skjuler teksten
     * @param shownField   tekstfeltet som viser teksten
     * @throws NullPointerException hvis en av parameterne er null
     */
    public static void connect(CheckBox showPassword, PasswordField hiddenField,
            TextField shownField) {
        Objects.requireNonNull(showPassword);
        Objects.requireNonNull(hiddenField);
        Objects.requireNonNull(shownField);

        shownField.textProperty().bindBidirectional(hiddenField.textProperty());
        shownField.disableProperty().bind(hiddenField.disableProperty());
        shownField.onActionProperty().bind(hiddenField.onActionProperty());

        BooleanExpression shown = showPassword.selectedProperty();
        shownField.visibleProperty().bind(shown);
        shownField.managedProperty().bind(shown);
        hiddenField.visibleProperty().bind(shown.not());
        hiddenField.managedProperty().bind(shown.not());
    }
}