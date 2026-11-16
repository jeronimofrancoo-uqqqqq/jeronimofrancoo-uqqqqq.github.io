module com.monedero {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.monedero.controller to javafx.fxml;
    opens com.monedero.domain.model to javafx.base;

    exports com.monedero.app;
    exports com.monedero.domain.model;
    exports com.monedero.domain.interfaces;
    exports com.monedero.domain.exception;
    exports com.monedero.repository;
    exports com.monedero.repository.memory;
    exports com.monedero.service;
    exports com.monedero.controller;
    exports com.monedero.util;
}
