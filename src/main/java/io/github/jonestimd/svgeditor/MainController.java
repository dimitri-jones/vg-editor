package io.github.jonestimd.svgeditor;

import java.io.File;

import io.github.jonestimd.svgeditor.svg.SvgParser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {
    @FXML
    private MenuBar menuBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Pane diagram;

    public void initialize() {
        scrollPane.setCursor(Cursor.CROSSHAIR);
        scrollPane.setPrefSize(600, 500);
        diagram.getChildren().add(new Circle(200, 200, 150));
    }

    public void createFile(ActionEvent event) {
        System.out.println("new file");
    }

    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("SVG files", "*.svg"),
                new ExtensionFilter("All files", "*.*"));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file != null) {
            try {
                diagram.getChildren().clear();
                diagram.getChildren().addAll(SvgParser.parse(file));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveFile(ActionEvent event) {
        System.out.println("save file");
    }

    public void saveFileAs(ActionEvent event) {
        System.out.println("save file as");
    }

    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }
}