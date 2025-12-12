import controller.Controller3D;
import view.Window;

public class App {
    public static void main(String[] args) {
        Window window = new Window(1000, 800);
        new Controller3D(window.getPanel());
    }
}