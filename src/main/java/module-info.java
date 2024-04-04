module org.discm.ballsserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.json;

    opens org.discm.ballsserver to javafx.fxml;
    exports org.discm.ballsserver;
}