module com.tambapps.p2p.fandem.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires fandem;

    opens com.tambapps.p2p.fandem.desktop to javafx.fxml;
    exports com.tambapps.p2p.fandem.desktop;
    opens com.tambapps.p2p.fandem.desktop.controller to javafx.fxml;
    exports com.tambapps.p2p.fandem.desktop.controller;

}