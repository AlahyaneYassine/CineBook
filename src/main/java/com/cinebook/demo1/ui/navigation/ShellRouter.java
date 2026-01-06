package com.cinebook.demo1.ui.navigation;

import javafx.scene.layout.StackPane;

public class ShellRouter {
    private static StackPane clientContentPane;

    public static void setClientContentPane(StackPane pane) {
        clientContentPane = pane;
    }

    public static StackPane getClientContentPane() {
        return clientContentPane;
    }
}
