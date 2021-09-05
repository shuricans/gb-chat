module ru.gb {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;

    opens ru.gb to javafx.fxml;
    exports ru.gb;
    exports ru.gb.logger;
    opens ru.gb.logger to javafx.fxml;
}