# lavienesa
script realizado en java para consultas sql de conciliacion de facturas electronicas de las 12 sucursales del los locales gastronomicos de la vienesa
permite conciliar si se enviaron todas las facturas por dia,semana,mes
y descargar el libro venta de todas las sucursales 
escalabilidad que permita hacer el reenvio automatico de las que no pasaron, se esta realizando manualmente por el momento.

se utilizo librerias 

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
y se creo un .bat y .jar para crear instalador con inno setup
