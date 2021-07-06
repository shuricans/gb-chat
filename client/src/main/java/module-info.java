module ru.gb {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.gb to javafx.fxml;
    exports ru.gb;
}