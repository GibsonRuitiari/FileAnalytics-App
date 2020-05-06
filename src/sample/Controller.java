package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
    public VBox vBoxLayout;
    public Text emptyFilesText;
    public Text duplicateFilesText;
    public Text emptyFoldersText;
    private ScheduledExecutorService scheduledExecutorService;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Text lastSearchTimeText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
           initializeScheduler();
           initializeMenuItem();
    }
    private void initializeScheduler(){
       scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
     scheduledExecutorService.schedule(this::loadData,3,TimeUnit.SECONDS);

    }

    private void loadData(){
        final int i = AppFunctionsHelper.checkEmptyDirectories();
        AppFunctionsHelper.checkEmptyFiles();
        try {
            AppFunctionsHelper.checkForDuplicateFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(()-> inflateData(i,AppFunctionsHelper.empty_files_count,AppFunctionsHelper.duplicate_files));
    }
    private void inflateData(int emptyDirectoryCount,int empty_files_count, int duplicateFiles){
        lastSearchTimeText.setText("The last analytics were done at: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/M/E . HH:mm:ss")));
        emptyFilesText.setText(String.format("The number of empty files in your pc: %d",empty_files_count));
        emptyFoldersText.setText(String.format("The number of empty folders in your pc are: %d", emptyDirectoryCount));
        duplicateFilesText.setText(String.format("The number of duplicate files in your pc are: %d",duplicateFiles));
        readjustStage(emptyFilesText.getScene().getWindow());
    }
    private void readjustStage(Window stage){
        stage.sizeToScene();
        Rectangle2D visualBounds= Screen.getPrimary().getVisualBounds();
        stage.setX(visualBounds.getMaxX()-25-emptyFilesText.getScene().getWidth());
        stage.setY(visualBounds.getMinY()+25);

    }
    private void initializeMenuItem(){
        MenuItem menuItem=new MenuItem();
        menuItem.setText("Exit");
        menuItem.setOnAction(event -> {
            AppFunctionsHelper.clearResources();
            if (!scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
            System.exit(0);
        });
        MenuItem refreshItem=new MenuItem();
        refreshItem.setText("Refresh analytics");
        refreshItem.setOnAction(event -> {
            scheduledExecutorService.schedule(this::loadData,2,TimeUnit.SECONDS);
        });
        final ContextMenu contextMenu=new ContextMenu(menuItem,
                refreshItem);
        rootPane.addEventFilter(MouseEvent.MOUSE_PRESSED,event -> {
            if (event.isSecondaryButtonDown()){
                contextMenu.show(rootPane,event.getScreenX(),event.getScreenY());
            }else{
                if (contextMenu.isShowing()){
                    contextMenu.hide();
                }
            }
        });
    }
}
