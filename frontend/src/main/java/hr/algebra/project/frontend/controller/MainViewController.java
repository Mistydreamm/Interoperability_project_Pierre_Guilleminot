package hr.algebra.project.frontend.controller;

import hr.algebra.project.frontend.enums.MuscleGroup;
import hr.algebra.project.frontend.model.PersonalRecord;
import hr.algebra.project.frontend.enums.LiftType;
import hr.algebra.project.frontend.service.PersonalRecordService;
import hr.algebra.project.frontend.GymPrClientApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MainViewController {

    private final PersonalRecordService recordService = new PersonalRecordService();
    private Long selectedRecordId;

    @FXML
    private Button addRecordButton;
    @FXML
    private Button editRecordButton;
    @FXML
    private Button showAddFormButton;
    @FXML
    private Button showEditFormButton;
    @FXML
    private Button deleteRecordButton;
    @FXML
    private TextField searchIdTextField;

    @FXML
    private TableView<PersonalRecord> recordsTableView;
    @FXML
    private TableColumn<PersonalRecord, LiftType> liftTypeColumn;
    @FXML
    private TableColumn<PersonalRecord, BigDecimal> weightColumn;
    @FXML
    private TableColumn<PersonalRecord, Integer> repsColumn;
    @FXML
    private TableColumn<PersonalRecord, LocalDate> dateColumn;
    @FXML
    private TableColumn<PersonalRecord, String> addedByColumn;

    @FXML
    private VBox postFormVBox;
    @FXML
    private ChoiceBox<LiftType> liftTypeChoiceBox;
    @FXML
    private ChoiceBox<MuscleGroup> muscleGroupChoiceBox;
    @FXML
    private TextField equipmentTextField;
    @FXML
    private TextField gymLocationTextField;
    @FXML
    private TextField weightTextField;
    @FXML
    private TextField repsTextField;
    @FXML
    private TextField setsTextField;
    @FXML
    private TextField rpeTextField;
    @FXML
    private TextField bodyweightTextField;
    @FXML
    private DatePicker liftDateDatePicker;
    @FXML
    private TextField notesTextField;
    @FXML
    private CheckBox milestoneCheckBox;
    @FXML
    private Button backupButton;
    @FXML
    private Button restoreButton;
    @FXML
    private TableColumn<PersonalRecord, String> gymLocationColumn;
    @FXML
    private TableColumn<PersonalRecord, String> equipmentColumn;
    @FXML
    private TableColumn<PersonalRecord, Boolean> milestoneColumn;
    @FXML
    private TableColumn<PersonalRecord, Integer> setsColumn;
    @FXML
    private TableColumn<PersonalRecord, BigDecimal> bodyweightColumn;
    @FXML
    private TableColumn<PersonalRecord, Integer> rpeColumn;
    @FXML
    private TableColumn<PersonalRecord, String> notesColumn;

    @FXML
    public void initialize() {
        liftTypeColumn.setCellValueFactory(new PropertyValueFactory<>("liftType"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weightKg"));
        repsColumn.setCellValueFactory(new PropertyValueFactory<>("reps"));
        setsColumn.setCellValueFactory(new PropertyValueFactory<>("sets"));
        rpeColumn.setCellValueFactory(new PropertyValueFactory<>("rpe"));
        bodyweightColumn.setCellValueFactory(new PropertyValueFactory<>("bodyweightKg"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("liftDate"));
        gymLocationColumn.setCellValueFactory(new PropertyValueFactory<>("gymLocation"));
        equipmentColumn.setCellValueFactory(new PropertyValueFactory<>("equipment"));
        milestoneColumn.setCellValueFactory(new PropertyValueFactory<>("milestone"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        addedByColumn.setCellValueFactory(new PropertyValueFactory<>("addedBy"));
        liftTypeChoiceBox.getItems().setAll(LiftType.values());
        muscleGroupChoiceBox.getItems().setAll(MuscleGroup.values());

        // Check user role and restrict access
        String userRole = getRoleFromToken(LoginRequestController.authToken);
        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        if (!isAdmin) {
            if (showAddFormButton != null) {
                showAddFormButton.setVisible(false);
                showAddFormButton.setManaged(false);
            }
            if (showEditFormButton != null) {
                showEditFormButton.setVisible(false);
                showEditFormButton.setManaged(false);
            }
            if (deleteRecordButton != null) {
                deleteRecordButton.setVisible(false);
                deleteRecordButton.setManaged(false);
            }
            if (backupButton != null) {
                backupButton.setVisible(false);
                backupButton.setManaged(false);
            }
            if (restoreButton != null) {
                restoreButton.setVisible(false);
                restoreButton.setManaged(false);
            }
        }

        loadPersonalRecords();
    }

    private void loadPersonalRecords() {
        try {
            final List<PersonalRecord> records = recordService.getAllRecords();
            recordsTableView.getItems().setAll(records);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load records: " + e.getMessage());
        }
    }

    @FXML
    private void togglePostForm() {
        boolean isVisible = postFormVBox.isVisible();
        postFormVBox.setVisible(!isVisible);
        postFormVBox.setManaged(!isVisible);
    }

    public PersonalRecord gatherPostInformation() {
        PersonalRecord informations = new PersonalRecord();
        informations.setLiftType(liftTypeChoiceBox.getValue());
        informations.setMuscleGroup(muscleGroupChoiceBox.getValue());
        informations.setEquipment(equipmentTextField.getText());
        informations.setWeightKg(new BigDecimal(weightTextField.getText()));
        informations.setReps(Integer.parseInt(repsTextField.getText()));
        informations.setSets(Integer.parseInt(setsTextField.getText()));
        informations.setRpe(Integer.parseInt(rpeTextField.getText()));
        informations.setBodyweightKg(new BigDecimal(bodyweightTextField.getText()));
        informations.setGymLocation(gymLocationTextField.getText());
        informations.setLiftDate(liftDateDatePicker.getValue());
        informations.setNotes(notesTextField.getText());
        informations.setMilestone(milestoneCheckBox.isSelected());
        return informations;
    }

    @FXML
    private void newPersonalRecord() {
        if (liftTypeChoiceBox.getValue() == null || muscleGroupChoiceBox.getValue() == null ||
                equipmentTextField.getText().isEmpty() || gymLocationTextField.getText().isEmpty() ||
                weightTextField.getText().isEmpty() || repsTextField.getText().isEmpty() ||
                setsTextField.getText().isEmpty() || rpeTextField.getText().isEmpty() ||
                bodyweightTextField.getText().isEmpty() ||
                liftDateDatePicker.getValue() == null || notesTextField.getText().isEmpty()) {
            return;
        }
        try {
            PersonalRecord record = gatherPostInformation();
            recordService.postRecord(record);
            loadPersonalRecords();

            togglePostForm();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Record added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add record: " + e.getMessage());
        }
    }

    @FXML
    private void editRecord() {

    }

    private void clearForm() {
        liftTypeChoiceBox.setValue(null);
        muscleGroupChoiceBox.setValue(null);
        equipmentTextField.clear();
        gymLocationTextField.clear();
        weightTextField.clear();
        repsTextField.clear();
        setsTextField.clear();
        rpeTextField.clear();
        bodyweightTextField.clear();
        liftDateDatePicker.setValue(null);
        notesTextField.clear();
        milestoneCheckBox.setSelected(false);
    }

    @FXML
    private void showAddForm() {
        clearForm();
        selectedRecordId = null;
        recordsTableView.getSelectionModel().clearSelection();

        addRecordButton.setVisible(true);
        addRecordButton.setManaged(true);
        editRecordButton.setVisible(false);
        editRecordButton.setManaged(false);

        if (!postFormVBox.isVisible()) {
            togglePostForm();
        }
    }

    @FXML
    private void showEditForm() {
        PersonalRecord selected = recordsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a personal record from the table first.");
            return;
        }

        selectedRecordId = selected.getId();
        fillForm(selected);

        addRecordButton.setVisible(false);
        addRecordButton.setManaged(false);
        editRecordButton.setVisible(true);
        editRecordButton.setManaged(true);

        if (!postFormVBox.isVisible()) {
            togglePostForm();
        }
    }

    @FXML
    private void searchRecordById() {
        String idText = searchIdTextField.getText();
        if (idText == null || idText.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Input", "Please enter a record ID in the search box.");
            return;
        }
        try {
            int id = Integer.parseInt(idText.trim());
            PersonalRecord record = recordService.getOneRecord(id);
            
            String details = "ID: " + record.getId() + "\n" +
                             "Lift Type: " + record.getLiftType() + "\n" +
                             "Muscle Group: " + record.getMuscleGroup() + "\n" +
                             "Weight: " + record.getWeightKg() + " kg\n" +
                             "Reps: " + record.getReps() + "\n" +
                             "Sets: " + record.getSets() + "\n" +
                             "RPE: " + record.getRpe() + "\n" +
                             "Bodyweight: " + record.getBodyweightKg() + " kg\n" +
                             "Date: " + record.getLiftDate() + "\n" +
                             "Location: " + (record.getGymLocation() != null ? record.getGymLocation() : "N/A") + "\n" +
                             "Equipment: " + (record.getEquipment() != null ? record.getEquipment() : "N/A") + "\n" +
                             "Milestone: " + (record.isMilestone() ? "Yes" : "No") + "\n" +
                             "Notes: " + (record.getNotes() != null ? record.getNotes() : "N/A") + "\n" +
                             "Added By: " + record.getAddedBy();

            showAlert(Alert.AlertType.INFORMATION, "Personal Record Details", details);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid numeric ID.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch record details: " + e.getMessage());
        }
    }

    private void fillForm(PersonalRecord record) {
        liftTypeChoiceBox.setValue(record.getLiftType());
        muscleGroupChoiceBox.setValue(record.getMuscleGroup());
        equipmentTextField.setText(record.getEquipment());
        gymLocationTextField.setText(record.getGymLocation());
        weightTextField.setText(record.getWeightKg() != null ? record.getWeightKg().toString() : "");
        repsTextField.setText(record.getReps() != null ? record.getReps().toString() : "");
        setsTextField.setText(record.getSets() != null ? record.getSets().toString() : "");
        rpeTextField.setText(record.getRpe() != null ? record.getRpe().toString() : "");
        bodyweightTextField.setText(record.getBodyweightKg() != null ? record.getBodyweightKg().toString() : "");
        liftDateDatePicker.setValue(record.getLiftDate());
        notesTextField.setText(record.getNotes());
        milestoneCheckBox.setSelected(record.isMilestone());
    }

    @FXML
    private void editPersonalRecord() {
        if (selectedRecordId == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a personal record to edit.");
            return;
        }
        if (liftTypeChoiceBox.getValue() == null || muscleGroupChoiceBox.getValue() == null ||
                equipmentTextField.getText().isEmpty() || gymLocationTextField.getText().isEmpty() ||
                weightTextField.getText().isEmpty() || repsTextField.getText().isEmpty() ||
                setsTextField.getText().isEmpty() || rpeTextField.getText().isEmpty() ||
                bodyweightTextField.getText().isEmpty() ||
                liftDateDatePicker.getValue() == null || notesTextField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all the fields.");
            return;
        }
        try {
            PersonalRecord record = gatherPostInformation();
            record.setId(selectedRecordId);

            recordService.putRecord(record, selectedRecordId.intValue());

            loadPersonalRecords();
            togglePostForm();
            clearForm();
            selectedRecordId = null;
            recordsTableView.getSelectionModel().clearSelection();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Record updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update record: " + e.getMessage());
        }
    }

    @FXML
    private void deletePersonalRecord() {
        PersonalRecord selected = recordsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a personal record to delete.");
            return;
        }
        try {
            recordService.deleteRecord(selected.getId().intValue());
            loadPersonalRecords();

            if (selectedRecordId != null && selectedRecordId.equals(selected.getId())) {
                togglePostForm();
                clearForm();
                selectedRecordId = null;
            }

            recordsTableView.getSelectionModel().clearSelection();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Record deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete record: " + e.getMessage());
        }
    }

    @FXML
    private void backupDatabase() {
        try {
            recordService.backupDatabase();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Database backup created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to backup database: " + e.getMessage());
        }
    }

    @FXML
    private void restoreDatabase() {
        try {
            recordService.restoreDatabase();
            loadPersonalRecords();
            selectedRecordId = null;
            recordsTableView.getSelectionModel().clearSelection();
            clearForm();
            if (postFormVBox.isVisible()) {
                togglePostForm();
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Database restored successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to restore database: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (LoginRequestController.refreshToken != null && !LoginRequestController.refreshToken.isEmpty()) {
                new hr.algebra.project.frontend.service.AuthService().logout(LoginRequestController.refreshToken);
            }
        } catch (Exception e) {
            System.err.println("Backend logout failed: " + e.getMessage());
        } finally {
            LoginRequestController.authToken = null;
            LoginRequestController.refreshToken = null;

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(GymPrClientApplication.class.getResource("loginView.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 320, 240);
                Stage stage = (Stage) backupButton.getScene().getWindow();
                stage.setTitle("Login");
                stage.setScene(scene);
                stage.show();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login screen.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getRoleFromToken(String token) {
        if (token == null || token.isEmpty()) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
            if (payload.contains("\"role\":\"")) {
                int start = payload.indexOf("\"role\":\"") + 8;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}