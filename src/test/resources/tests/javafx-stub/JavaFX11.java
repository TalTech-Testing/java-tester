import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaFX11 extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(new StackPane(new Label("This should compile")), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}