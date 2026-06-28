module hr.algebra.project.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens hr.algebra.project.frontend to javafx.fxml;
    exports hr.algebra.project.frontend;
    exports hr.algebra.project.frontend.controller;
    opens hr.algebra.project.frontend.controller to javafx.fxml;
    exports hr.algebra.project.frontend.model;
    opens hr.algebra.project.frontend.model to com.fasterxml.jackson.databind;
    exports hr.algebra.project.frontend.enums;
    opens hr.algebra.project.frontend.enums to com.fasterxml.jackson.databind;
}
