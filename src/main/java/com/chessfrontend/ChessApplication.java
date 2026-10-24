package com.chessfrontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class ChessApplication extends Application {

    private static final String LIGHT_SQUARE_COLOR = "#F0D9B5";
    private static final String DARK_SQUARE_COLOR = "#B58863";

    private final StackPane[][] squares = new StackPane[8][8];
    private int selectedRow = -1;
    private int selectedColumn = -1;

    private Label turnLabel;
    private Label feedbackLabel;
    private Label historyLabel;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1F2937;");
        root.setPadding(new Insets(24));

        Label title = new Label("Chess Game");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Frontend preview • Backend not connected");
        subtitle.setTextFill(Color.web("#C9D2E3"));
        subtitle.setFont(Font.font(15));

        VBox heading = new VBox(4, title, subtitle);

        Button newGameButton = createButton("New Game");
        newGameButton.setOnAction(event -> resetFrontend("New game requested. Connect the backend to create a real game."));

        Button resetButton = createButton("Reset Board");
        resetButton.setOnAction(event -> resetFrontend("Board selection cleared. A backend will control real resets."));

        HBox topBar = new HBox(16, heading, newGameButton, resetButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        GridPane chessboard = createChessboard();
        VBox sidePanel = createSidePanel();

        HBox content = new HBox(28, chessboard, sidePanel);
        content.setAlignment(Pos.TOP_CENTER);

        root.setTop(topBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1_000, 760);
        stage.setTitle("Chess Game");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.show();
    }

    private VBox createSidePanel() {
        Label statusTitle = sectionTitle("Game Status");

        turnLabel = new Label("White to move");
        turnLabel.setTextFill(Color.web("#D1FAE5"));
        turnLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label backendStatus = new Label("Waiting for chess backend");
        backendStatus.setTextFill(Color.web("#FCD34D"));
        backendStatus.setFont(Font.font(14));

        Label feedbackTitle = sectionTitle("Message");
        feedbackLabel = new Label("Select a square to preview the interface.");
        feedbackLabel.setTextFill(Color.web("#C9D2E3"));
        feedbackLabel.setWrapText(true);
        feedbackLabel.setFont(Font.font(14));

        Label historyTitle = sectionTitle("Move History");
        historyLabel = new Label("No moves yet. Your backend will provide confirmed moves.");
        historyLabel.setTextFill(Color.web("#C9D2E3"));
        historyLabel.setWrapText(true);
        historyLabel.setFont(Font.font(14));

        VBox panel = new VBox(12, statusTitle, turnLabel, backendStatus,
                feedbackTitle, feedbackLabel, historyTitle, historyLabel);
        panel.setPrefWidth(250);
        panel.setMaxWidth(250);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #111827; -fx-background-radius: 12;");
        return panel;
    }

    private Label sectionTitle(String text) {
        Label title = new Label(text);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        return title;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 8; -fx-padding: 10 16;");
        return button;
    }

    private GridPane createChessboard() {
        GridPane board = new GridPane();
        board.setPrefSize(620, 620);
        board.setMinSize(620, 620);
        board.setMaxSize(620, 620);
        board.setStyle("-fx-background-color: #111827; -fx-padding: 8; -fx-background-radius: 12;");

        for (int index = 0; index < 8; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(12.5);
            column.setHgrow(Priority.ALWAYS);
            board.getColumnConstraints().add(column);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(12.5);
            row.setVgrow(Priority.ALWAYS);
            board.getRowConstraints().add(row);
        }

        String[][] pieces = createStartingPieces();
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                StackPane square = createSquare(row, column, pieces[row][column]);
                squares[row][column] = square;
                board.add(square, column, row);
            }
        }
        return board;
    }

    private StackPane createSquare(int row, int column, String pieceImageName) {
        StackPane square = new StackPane();
        square.setStyle(squareColorStyle(row, column));
        square.setOnMouseClicked(event -> handleSquareClick(row, column));

        if (!pieceImageName.isEmpty()) {
            square.getChildren().add(createChessPiece(pieceImageName));
        }

        if (column == 0) {
            Label rank = createCoordinateLabel(String.valueOf(8 - row));
            StackPane.setAlignment(rank, Pos.TOP_LEFT);
            square.getChildren().add(rank);
        }

        if (row == 7) {
            Label file = createCoordinateLabel(String.valueOf((char) ('a' + column)));
            StackPane.setAlignment(file, Pos.BOTTOM_RIGHT);
            square.getChildren().add(file);
        }

        return square;
    }

    private Label createCoordinateLabel(String text) {
        Label coordinate = new Label(text);
        coordinate.setPadding(new Insets(3, 6, 3, 6));
        coordinate.setTextFill(Color.web("#5B3B25"));
        coordinate.setFont(Font.font("System", FontWeight.BOLD, 12));
        coordinate.setMouseTransparent(true);
        return coordinate;
    }

    private void handleSquareClick(int row, int column) {
        if (selectedRow == -1) {
            selectSquare(row, column);
            feedbackLabel.setText("Selected " + squareName(row, column)
                    + ". Choose a destination square. The backend will validate the move later.");
            return;
        }

        if (selectedRow == row && selectedColumn == column) {
            clearSelection();
            feedbackLabel.setText("Selection cleared.");
            return;
        }

        String from = squareName(selectedRow, selectedColumn);
        String to = squareName(row, column);
        clearSelection();
        feedbackLabel.setText("Move request " + from + " → " + to
                + " cannot be applied until the backend is connected.");
    }

    private void selectSquare(int row, int column) {
        clearSelection();
        selectedRow = row;
        selectedColumn = column;
        squares[row][column].setStyle(squareColorStyle(row, column)
                + " -fx-border-color: #38BDF8; -fx-border-width: 4;"
                + " -fx-border-insets: 5; -fx-border-radius: 4;");
    }

    private void clearSelection() {
        if (selectedRow != -1) {
            squares[selectedRow][selectedColumn].setStyle(squareColorStyle(selectedRow, selectedColumn));
        }
        selectedRow = -1;
        selectedColumn = -1;
    }

    private String squareColorStyle(int row, int column) {
        boolean isLightSquare = (row + column) % 2 == 0;
        String color = isLightSquare ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR;
        return "-fx-background-color: " + color + ";";
    }

    private Group createChessPiece(String imageName) {
        String resourcePath = "/com/chessfrontend/pieces/" + imageName;

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document document = factory.newDocumentBuilder().parse(inputStream);
            NodeList pathNodes = document.getElementsByTagName("path");
            Group piece = new Group();
            boolean isBlackPiece = imageName.startsWith("black");

            for (int index = 0; index < pathNodes.getLength(); index++) {
                Element pathElement = (Element) pathNodes.item(index);
                SVGPath path = new SVGPath();
                path.setContent(pathElement.getAttribute("d"));
                stylePieceShape(path, pathElement, isBlackPiece);
                piece.getChildren().add(path);
            }

            NodeList circleNodes = document.getElementsByTagName("circle");
            for (int index = 0; index < circleNodes.getLength(); index++) {
                Element circleElement = (Element) circleNodes.item(index);
                Circle circle = new Circle(
                        readNumber(circleElement, "cx", 0),
                        readNumber(circleElement, "cy", 0),
                        readNumber(circleElement, "r", 0));
                stylePieceShape(circle, circleElement, isBlackPiece);
                piece.getChildren().add(circle);
            }

            piece.setScaleX(1.52);
            piece.setScaleY(1.52);
            piece.setMouseTransparent(true);
            return piece;
        } catch (Exception exception) {
            Label unavailablePiece = new Label("?");
            unavailablePiece.setTextFill(Color.RED);
            return new Group(unavailablePiece);
        }
    }

    private void stylePieceShape(Shape shape, Element element, boolean isBlackPiece) {
        shape.setFill(readPaint(element, "fill", isBlackPiece ? Color.web("#444444") : Color.WHITE));
        shape.setStroke(readPaint(element, "stroke", Color.web("#303030")));
        shape.setStrokeWidth(readNumber(element, "stroke-width", 1.5));
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
        shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
        shape.setMouseTransparent(true);
    }

    private Paint readPaint(Element element, String propertyName, Paint defaultPaint) {
        String value = readStyleValue(element, propertyName);
        if (value == null || value.equals("none")) {
            return value == null ? defaultPaint : null;
        }
        return Color.web(value);
    }

    private double readNumber(Element element, String propertyName, double defaultValue) {
        String value = readStyleValue(element, propertyName);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    private String readStyleValue(Element element, String propertyName) {
        Node currentNode = element;
        while (currentNode instanceof Element currentElement) {
            if (currentElement.hasAttribute(propertyName)) {
                return currentElement.getAttribute(propertyName);
            }

            String style = currentElement.getAttribute("style");
            for (String declaration : style.split(";")) {
                String[] nameAndValue = declaration.trim().split(":", 2);
                if (nameAndValue.length == 2 && nameAndValue[0].trim().equals(propertyName)) {
                    return nameAndValue[1].trim();
                }
            }
            currentNode = currentNode.getParentNode();
        }
        return null;
    }

    private void resetFrontend(String message) {
        clearSelection();
        turnLabel.setText("White to move");
        historyLabel.setText("No moves yet. Your backend will provide confirmed moves.");
        feedbackLabel.setText(message);
    }

    private String squareName(int row, int column) {
        char file = (char) ('a' + column);
        int rank = 8 - row;
        return "" + file + rank;
    }

    private String[][] createStartingPieces() {
        return new String[][]{
                {"black-rook.svg", "black-knight.svg", "black-bishop.svg", "black-queen.svg",
                        "black-king.svg", "black-bishop.svg", "black-knight.svg", "black-rook.svg"},
                {"black-pawn.svg", "black-pawn.svg", "black-pawn.svg", "black-pawn.svg",
                        "black-pawn.svg", "black-pawn.svg", "black-pawn.svg", "black-pawn.svg"},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""},
                {"white-pawn.svg", "white-pawn.svg", "white-pawn.svg", "white-pawn.svg",
                        "white-pawn.svg", "white-pawn.svg", "white-pawn.svg", "white-pawn.svg"},
                {"white-rook.svg", "white-knight.svg", "white-bishop.svg", "white-queen.svg",
                        "white-king.svg", "white-bishop.svg", "white-knight.svg", "white-rook.svg"}
        };
    }
}
