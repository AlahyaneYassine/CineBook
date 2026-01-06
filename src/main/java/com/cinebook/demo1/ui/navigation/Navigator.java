package com.cinebook.demo1.ui.navigation;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Centralized navigation manager for CineBook application.
 * Handles scene switching with smooth animations.
 */
public class Navigator {
    
    private static final String CSS_PATH = "/com/cinebook/demo1/styles/app.css";
    private static Stage primaryStage;
    
    /**
     * Initialize the Navigator with the primary stage.
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
    }
    
    /**
     * Navigate to a new scene with fade animation.
     */
    public static void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, null, null);
    }
    
    /**
     * Navigate to a new scene with custom dimensions and fade animation.
     */
    public static void navigateTo(String fxmlPath, String title, Double width, Double height) {
        try {
            // Use getClassLoader() for absolute paths (more reliable)
            java.net.URL resourceUrl = Navigator.class.getClassLoader().getResource(fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Scene scene;
            
            if (width != null && height != null) {
                scene = new Scene(loader.load(), width, height);
            } else {
                scene = new Scene(loader.load());
            }
            
            applyStyles(scene);
            
            if (primaryStage != null) {
                // Animate scene transition
                animateSceneTransition(primaryStage.getScene(), scene);
                
                primaryStage.setTitle(title);
                primaryStage.setScene(scene);
                primaryStage.show();
            } else {
                throw new IllegalStateException("Navigator not initialized. Call Navigator.initialize(stage) first.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load: " + fxmlPath + "\n" + e.getMessage());
        }
    }
    
    /**
     * Navigate using a Node from the current scene.
     */
    public static void navigateFromNode(Node node, String fxmlPath, String title) {
        navigateFromNode(node, fxmlPath, title, null, null);
    }
    
    /**
     * Navigate using a Node with custom dimensions and animation.
     */
    public static void navigateFromNode(Node node, String fxmlPath, String title, Double width, Double height) {
        try {
            // Use getClassLoader() for absolute paths (more reliable)
            java.net.URL resourceUrl = Navigator.class.getClassLoader().getResource(fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Scene scene;
            
            if (width != null && height != null) {
                scene = new Scene(loader.load(), width, height);
            } else {
                scene = new Scene(loader.load());
            }
            
            applyStyles(scene);
            
            Stage stage = (Stage) node.getScene().getWindow();
            
            // Animate scene transition
            animateSceneTransition(stage.getScene(), scene);
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load: " + fxmlPath + "\n" + e.getMessage());
        }
    }
    
    /**
     * Load content into a StackPane (for shell-based navigation).
     * Returns the loaded Node for animation.
     */
    public static Node loadContent(String fxmlPath, StackPane container) {
        try {
            // Use getClassLoader() for absolute paths (more reliable)
            java.net.URL resourceUrl = Navigator.class.getClassLoader().getResource(fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node content = loader.load();
            
            // Animate content load
            animateContentLoad(content, container);
            
            container.getChildren().clear();
            container.getChildren().add(content);
            
            return content;
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Content Load Error", "Failed to load: " + fxmlPath + "\n" + e.getMessage());
            return null;
        }
    }
    
    /**
     * Animate scene transition with fade effect.
     */
    private static void animateSceneTransition(Scene oldScene, Scene newScene) {
        if (oldScene != null && oldScene.getRoot() != null) {
            // Fade out old scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Fade in new scene
                if (newScene.getRoot() != null) {
                    newScene.getRoot().setOpacity(0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            });
            fadeOut.play();
        } else if (newScene.getRoot() != null) {
            // Just fade in if no old scene
            newScene.getRoot().setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }
    
    /**
     * Animate content load with fade and slight translate.
     */
    private static void animateContentLoad(Node content, StackPane container) {
        content.setOpacity(0);
        content.setTranslateX(-20);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), content);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), content);
        slideIn.setFromX(-20);
        slideIn.setToX(0);
        
        fadeIn.play();
        slideIn.play();
    }
    
    /**
     * Open a modal window.
     */
    public static Stage openModal(String fxmlPath, String title, double width, double height) {
        return openModal(fxmlPath, title, width, height, null);
    }
    
    /**
     * Open a modal window with owner stage.
     */
    public static Stage openModal(String fxmlPath, String title, double width, double height, Stage owner) {
        try {
            // Use getClassLoader() for absolute paths (more reliable)
            java.net.URL resourceUrl = Navigator.class.getClassLoader().getResource(fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath);
            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Scene scene = new Scene(loader.load(), width, height);
            applyStyles(scene);
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            if (owner != null) {
                stage.initOwner(owner);
            }
            
            // Animate modal appearance
            scene.getRoot().setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            stage.show();
            fadeIn.play();
            
            return stage;
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Modal Error", "Failed to load: " + fxmlPath + "\n" + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply CSS styles to a scene.
     */
    private static void applyStyles(Scene scene) {
        java.net.URL cssUrl = Navigator.class.getClassLoader().getResource(CSS_PATH.startsWith("/") ? CSS_PATH.substring(1) : CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
    
    /**
     * Show error alert.
     */
    private static void showError(String header, String message) {
        javafx.scene.control.Alert alert = 
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Get the primary stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
