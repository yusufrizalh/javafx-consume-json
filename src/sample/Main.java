package sample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    // semua kebutuhan untuk consume json
    private static final String JSON_URL = "https://api.myjson.com/bins/3jwmh";
    private static final String IMAGE_URL = "https://www.fontspring.com/presentation/images/ajax_loader_blue_512.gif";
    private final ExecutorService executorService =
            Executors.newCachedThreadPool();
    private Image loadImage;
    private ObservableList<Products> listOfProducts;

    @Override
    public void start(Stage stage) throws Exception {
        loadImage = new Image(IMAGE_URL);
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setSpacing(20);

        Button openDataJsonBtn = new Button("Open Data JSON");

        root.getChildren().addAll(openDataJsonBtn);

        openDataJsonBtn.setOnAction(e -> {
            // menampilkan gambar loading
            ImageView loading = new ImageView(loadImage);
            loading.setFitWidth(60);
            loading.setFitHeight(60);
            root.getChildren().add(loading);

            // mengambil data json dari url
            executorService.submit(fetchListProducts);
        });

        fetchListProducts.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                listOfProducts = FXCollections.observableArrayList(
                        fetchListProducts.getValue()
                );

                // tampilkan semua value kedalam gridpane, tableview
                GridPane gridPane = createGridPane(listOfProducts);
                root.getChildren().remove(1);   // menghilangkan loading
                VBox.setVgrow(gridPane, Priority.ALWAYS);
                root.getChildren().add(gridPane);
                stage.sizeToScene();
            }
        });

        ScrollPane scrollPane = new ScrollPane(root);
        Scene scene = new Scene(scrollPane, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Load Data JSON");
        stage.show();
    }

    public GridPane createGridPane(ObservableList<Products> listOfProducts) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setGridLinesVisible(true);
        gridPane.setPadding(new Insets(20));
        gridPane.setMinHeight(500);
        gridPane.setMinWidth(500);

        // menyisipkan css

        // membuat headings
        Label nameHeading = new Label("Name");
        Label priceHeading = new Label("Price");
        Label imageHeading = new Label("Image");

        nameHeading.setStyle("-fx-font-weight: bold");
        priceHeading.setStyle("-fx-font-weight: bold");
        imageHeading.setStyle("-fx-font-weight: bold");

        // memposisikan masing-masing grid
        gridPane.add(nameHeading, 0, 0);
        gridPane.add(priceHeading, 1, 0);
        gridPane.add(imageHeading, 2, 0);

        // ------------------------------------

        for (int i = 0; i < listOfProducts.size(); i++) {
            Products products = listOfProducts.get(i);

            Label nameLabel = new Label(products.getName());
            Label priceLabel = new Label(products.getPrice().toString());
            ImageView imageView = new ImageView(loadImage);
                imageView.setFitHeight(60);
                imageView.setFitWidth(60);

            // mengambil image

            // letakkan semua kedalam grid
            gridPane.add(nameLabel, 0, i + 1);
            gridPane.add(priceLabel, 1, i + 1);
            gridPane.add(imageView, 2, i + 1);
        }

        return gridPane;
    }

    private Task<List<Products>> fetchListProducts = new Task() {
        @Override
        protected List<Products> call() throws Exception {
            List<Products> list = null;
            try {
                Gson gson = new Gson();
                list = new Gson().fromJson(readUrl(JSON_URL),
                        new TypeToken<List<Products>>() {
                        }.getType());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return list;
        }
    };

    private static String readUrl(String urlString) throws IOException {
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int baca;
            char[] chars = new char[1024];

            while ((baca = reader.read(chars)) != -1)
                buffer.append(chars, 0, baca);
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private class Products {
        private final String name;
        private final Double price;
        private final String imageUrl;

        public Products(String name, Double price, String imageUrl) {
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public Double getPrice() {
            return price;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
