package com.cinebook.demo1.ui.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Toast notification utility for showing temporary messages.
 */
public class Toast {
    
    public enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }
    
    /**
     * Show a toast notification.
     */
    public static void show(Stage owner, String message, ToastType type) {
        show(owner, message, type, 3000);
    }
    
    /**
     * Show a toast notification with custom duration.
     */
    public static void show(Stage owner, String message, ToastType type, int durationMs) {
        // Create toast content
        Label label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600;");
        
        VBox toastBox = new VBox(label);
        toastBox.setAlignment(Pos.CENTER);
        toastBox.setSpacing(8);
        toastBox.setStyle("-fx-padding: 16px 24px; -fx-background-radius: 16px; -fx-min-width: 300px; -fx-max-width: 500px;");
        
        // Apply type-specific styling
        String styleClass = "cb-toast";
        switch (type) {
            case SUCCESS:
                styleClass = "cb-toast-success";
                break;
            case ERROR:
                styleClass = "cb-toast-error";
                break;
            case WARNING:
                styleClass = "cb-toast-warning";
                break;
            case INFO:
                styleClass = "cb-toast-info";
                break;
        }
        toastBox.getStyleClass().add(styleClass);
        
        // Create popup
        Popup popup = new Popup();
        popup.getContent().add(toastBox);
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        
        // Position at top-center
        if (owner != null && owner.isShowing()) {
            double x = owner.getX() + (owner.getWidth() / 2) - 150;
            double y = owner.getY() + 80;
            popup.show(owner, x, y);
        } else {
            popup.show(owner);
        }
        
        // Animate appearance
        toastBox.setOpacity(0);
        toastBox.setTranslateY(-20);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastBox);
        slideIn.setFromY(-20);
        slideIn.setToY(0);
        
        // Animate disappearance
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastBox);
        slideOut.setFromY(0);
        slideOut.setToY(-20);
        
        SequentialTransition fadeOutSequence = new SequentialTransition(
            fadeOut, slideOut
        );
        fadeOutSequence.setOnFinished(e -> popup.hide());
        
        // Pause then fade out
        PauseTransition pause = new PauseTransition(Duration.millis(durationMs));
        pause.setOnFinished(e -> fadeOutSequence.play());
        
        // Play animations
        fadeIn.play();
        slideIn.play();
        pause.play();
    }
    
    /**
     * Show success toast.
     */
    public static void showSuccess(Stage owner, String message) {
        show(owner, message, ToastType.SUCCESS);
    }
    
    /**
     * Show error toast.
     */
    public static void showError(Stage owner, String message) {
        show(owner, message, ToastType.ERROR);
    }
    
    /**
     * Show warning toast.
     */
    public static void showWarning(Stage owner, String message) {
        show(owner, message, ToastType.WARNING);
    }
    
    /**
     * Show info toast.
     */
    public static void showInfo(Stage owner, String message) {
        show(owner, message, ToastType.INFO);
    }
}

