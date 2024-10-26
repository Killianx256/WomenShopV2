package com.example.womenshop.womenshopv2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WomenShopApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WomenShopApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("WomenShop Application !");
        stage.setScene(scene);
        setWindowSize(stage);
        stage.show();
    }

    public void setWindowSize(Stage stage) {
        stage.setWidth(770);
        stage.setHeight(800);
        stage.setResizable(false);
    }

    public void switchToView(Stage stage, Parent newView) {
        Scene scene = new Scene(newView);
        stage.setScene(scene);
        setWindowSize(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}