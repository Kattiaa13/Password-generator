import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.nio.file.StandardCopyOption;
import java.io.BufferedWriter;


public class PasswordManager extends Application {

    private TextField nameField;
    private TextField usernameField;
    private ComboBox<String> resourceComboBox;
    private TextField customResourceField;
    private TextArea resultArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Password Manager");

        Label nameLabel = new Label("Enter your Name");
        nameLabel.setStyle("-fx-font-size: 16;");
        nameField = new TextField();
        nameField.setPromptText("Enter your Name");
        nameField.setStyle("-fx-font-size: 16;");

        Label usernameLabel = new Label("Enter your username");
        usernameLabel.setStyle("-fx-font-size: 16;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-font-size: 16;");

        Label resourceLabel = new Label("Select the resource for which you want to create a password:");
        resourceLabel.setStyle("-fx-font-size: 16;");

        resourceComboBox = new ComboBox<>();
        resourceComboBox.getItems().addAll("Telegram", "Instagram", "Gmail", "Apple ID", "Facebook", "Other");
        resourceComboBox.setValue("Telegram");
        resourceComboBox.setStyle("-fx-font-size: 16;");

        customResourceField = new TextField();
        customResourceField.setPromptText("Enter custom resource");
        customResourceField.setStyle("-fx-font-size: 16;");
        customResourceField.setDisable(true);

        resourceComboBox.setOnAction(e -> {
            if (resourceComboBox.getValue().equals("Other")) {
                customResourceField.setDisable(false);
            } else {
                customResourceField.setDisable(true);
            }
        });

        Button generateButton = new Button("Generate Password");
        Button retrieveButton = new Button("Retrieve Password");
        Button deleteButton = new Button("Delete Password");

        generateButton.setStyle("-fx-font-size: 16; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        retrieveButton.setStyle("-fx-font-size: 16; -fx-background-color: #008CBA; -fx-text-fill: white;");
        deleteButton.setStyle("-fx-font-size: 16; -fx-background-color: #f44336; -fx-text-fill: white;");

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setStyle("-fx-font-size: 16;");

        generateButton.setOnAction(e -> generatePassword());
        retrieveButton.setOnAction(e -> retrievePassword());
        deleteButton.setOnAction(e -> deletePassword());

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.getChildren().addAll(
                new VBox(nameLabel, nameField),
                new VBox(usernameLabel, usernameField),
                resourceLabel,
                resourceComboBox, customResourceField,
                generateButton, retrieveButton, deleteButton, resultArea
        );

        Scene scene = new Scene(layout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void generatePassword() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String selectedResource = resourceComboBox.getValue().equals("Other") ? customResourceField.getText().trim() : resourceComboBox.getValue();

        if (!name.isEmpty() && !username.isEmpty()) {
            String password = generateRandomPassword();
            savePassword(name, username, selectedResource, password);
            resultArea.setText("Generated password: " + password);
        } else {
            resultArea.setText("Please enter your Name and username.");
        }
    }

    private String generateRandomPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()-_+=<>?";

        String allChars = uppercase + lowercase + digits + symbols;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(allChars.charAt(random.nextInt(allChars.length())));

        for (int i = 4; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        return password.toString();
    }

    private void savePassword(String name, String username, String resource, String password) {
        try {
            // Використовуємо ім'я ресурсу для формування імені файлу
            String filename = resource.replaceAll("\\s", "") + ".txt";
            FileWriter writer = new FileWriter(filename, true);
            writer.write(name + ":" + username + ":" + resource + ":" + password + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retrievePassword() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String selectedResource = resourceComboBox.getValue().equals("Other") ? customResourceField.getText().trim() : resourceComboBox.getValue();

        if (!name.isEmpty() && !username.isEmpty()) {
            try {
                String password = getPasswordFromFile(name, username, selectedResource);
                if (password != null) {
                    resultArea.setText("Retrieved password: " + password);
                } else {
                    resultArea.setText("Combination of Name, username, and resource not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            resultArea.setText("Please enter your Name and username.");
        }
    }

    private String getPasswordFromFile(String name, String username, String resource) throws IOException {
        File inputFile = new File(resource.replaceAll("\\s", "") + ".txt");
        if (inputFile.exists()) {
            return Files.lines(inputFile.toPath())
                    .filter(line -> line.startsWith(name + ":" + username + ":"))
                    .findFirst()
                    .map(line -> line.split(":")[3])
                    .orElse(null);
        } else {
            return null;
        }
    }

    private boolean deletePasswordFromFile(String username, String resource) {
        File inputFile = new File(resource.replaceAll("\\s", "") + ".txt");
        File tempFile = new File("temp.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            Files.lines(inputFile.toPath())
                    .filter(line -> !line.contains(username + ":"))
                    .forEach(line -> {
                        try {
                            writer.write(line + System.lineSeparator());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            // Використовуємо метод копіювання файлу
            Files.copy(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Видаляємо тимчасовий файл
        return tempFile.delete();
    }

    private void deletePassword() {
        String username = usernameField.getText().trim();
        String resource = resourceComboBox.getValue();
        if (!username.isEmpty() && resource != null) {
            boolean deleted = deletePasswordFromFile(username, resource);
            if (deleted) {
                resultArea.setText("Deleted username: " + username);
            } else {
                resultArea.setText("Username not found.");
            }
        } else {
            resultArea.setText("Please enter a username and select a resource.");
        }
    }
}