package goksoft.chat.app;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Function2 {

    public static BorderPane chatBorderPane;
    public static BorderPane settingsBorderPane;
    public static ScrollPane friendScrollPane;
    public static HBox operationsHBox;
    public static VBox friendSection;
    public static VBox mailboxSection;
    public static VBox addFriendSection;
    public static VBox friendsVBox;
    public static VBox notificationVBox;
    public static VBox usersVBox;
    public static VBox settingsTopVBox;
    public static TextField searchUserField;
    public static TextField searchFriendField;
    public static Circle profilePhoto;
    public static Circle settingsButton;
    public static Circle chatFriendProfilePhoto;
    public static Label chatFriendName;
    public static TextField messageField;
    public static ListView<String> listView;
    public static ChoiceBox<String> languageChoiceBox;
    public static String currentFriend; //Chatted friend
    public static BorderPane currentPane; //Chatted friend's pane
    public static ArrayList<String> friendsNameList; //Stores friends' names to search over later.
    public static List<Object> friendArray; //
    public static Label noFriendLabel;
    public static Label noNotifLabel;
    public static Label noUserLabel;
    public static RadioButton darkThemeButton;

    public static int currentTimer;

    //Constructor of the Function2 class
    public Function2(BorderPane chatBorderPane, BorderPane settingsBorderPane, HBox operationsHBox, ScrollPane friendScrollPane,VBox friendSection, VBox mailboxSection, VBox addFriendSection,
                     VBox friendsVBox, VBox notificationVBox, VBox usersVBox,VBox settingsTopVBox, TextField searchUserField,TextField searchFriendField, Circle profilePhoto, Circle settingsButton,
                     Circle chatFriendProfilePhoto, Label chatFriendName, TextField messageField,
                     ListView<String> listView, ChoiceBox<String> languageChoiceBox, String currentFriend, ArrayList<String> friendsNameList,
                     List<Object> friendArray,Label noFriendLabel,Label noNotifLabel,Label noUserLabel,RadioButton darkThemeButton){
        Function2.chatBorderPane = chatBorderPane;
        Function2.settingsBorderPane = settingsBorderPane;
        Function2.operationsHBox = operationsHBox;
        Function2.friendScrollPane = friendScrollPane;
        Function2.friendSection = friendSection;
        Function2.mailboxSection = mailboxSection;
        Function2.addFriendSection = addFriendSection;
        Function2.friendsVBox = friendsVBox;
        Function2.notificationVBox = notificationVBox;
        Function2.usersVBox = usersVBox;
        Function2.settingsTopVBox = settingsTopVBox;
        Function2.searchUserField = searchUserField;
        Function2.searchFriendField = searchFriendField;
        Function2.profilePhoto = profilePhoto;
        Function2.settingsButton = settingsButton;
        Function2.chatFriendProfilePhoto = chatFriendProfilePhoto;
        Function2.chatFriendName = chatFriendName;
        Function2.messageField = messageField;
        Function2.listView = listView;
        Function2.languageChoiceBox = languageChoiceBox;
        Function2.currentFriend = currentFriend;
        Function2.friendsNameList = friendsNameList;
        Function2.friendArray = friendArray;
        Function2.noFriendLabel = noFriendLabel;
        Function2.noNotifLabel = noNotifLabel;
        Function2.noUserLabel = noUserLabel;
        Function2.darkThemeButton = darkThemeButton;
    }

    public static void getClickedFriend(Image friendPhoto, String friendName, BorderPane pane){

        Function2.chatFriendName.setText(friendName);
        Function2.chatFriendProfilePhoto.setFill(new ImagePattern(friendPhoto));
        Function2.chatFriendProfilePhoto.setStrokeWidth(0);
        Function2.chatBorderPane.setVisible(true);
        Function2.currentFriend = friendName;
        Function2.currentPane = pane;

        final String curFriend = ServerFunctions.encodeURL(currentFriend);
        Thread thread = new Thread(()-> {
            currentTimer = (int) (Math.random() * 1000);
            int selv = currentTimer;
                while(selv == currentTimer) {
                    try {
                        /*Get notification count from the server. If count is more than 0, it means it has been sent a message from opponent side.
                        Get all messages.*/

                        String cevap = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/checkNotif.php", "chatter=" + curFriend);
                        Thread.sleep(1000);
                        System.out.println("checknotif: " + cevap);
                        if (!cevap.equals("0"))
                            Platform.runLater(Function2::getMessages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        });
        thread.start();
        Function2.chatBorderPane.getScene().getWindow().setOnCloseRequest(e -> System.exit(0));
    }

    //Show no match labels.
    public static void checkNoResult(String string,Label label){
        if (string.equals("[]")){
            System.out.println("empty");
            label.setManaged(true);
            label.setVisible(true);
        } else {
            System.out.println("no empty");
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    //Get logged in user's profile photo
    public static void getProfilePhoto(boolean mouseEvent){
        final String imageName = ServerFunctions.encodeURL(LoginController.loggedUser);
        Thread thread = new Thread(() -> {
            try {
                //Get the image from server and convert it to BufferedImage to use in JavaFX
                BufferedImage image = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + imageName));
                Image imagefx = SwingFXUtils.toFXImage(image, null);

                Platform.runLater(() -> {
                    if (imagefx.isError()){ //Fill profile photo in blue if image has error
                        profilePhoto.setFill(Color.DODGERBLUE);
                    } else {
                        profilePhoto.setFill(new ImagePattern(imagefx));
                        settingsButton.setFill(new ImagePattern(imagefx)); //Set image into the circle at bottom-left.
                    }
                    //Fill profile photo with black when mouseEvent is true
                    if (mouseEvent){
                        profilePhoto.setFill(Color.BLACK);
                        Tooltip.install(
                                profilePhoto,
                                new Tooltip("Change Profile Photo")
                        );
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    //updates the friend's borderpane with the new datas.
    public static  void updateFriendStatus(){
        Thread thread = new Thread(() -> {
            BufferedImage image;
            try {
                image = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + currentFriend));
                Image imagefx = SwingFXUtils.toFXImage(image, null);
                String msg = listView.getItems().get(listView.getItems().size() -1);
                int index = msg.indexOf(':');
                String lastmsg = msg.substring(index+1);
                Platform.runLater(() -> {
                    friendsVBox.getChildren().remove(currentPane);
                    BorderPane pane = friendBox(imagefx,currentFriend,lastmsg,"0","Now");
                    friendsVBox.getChildren().add(0, pane);
                    currentPane = pane;
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void getFriends(){
        friendsNameList = new ArrayList<>();
        friendArray = new ArrayList<>();
        friendsVBox.getChildren().clear();
        friendsNameList.clear();

        Thread thread = new Thread(() -> {
            try {
                String stringArray = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/getFriends.php", ""); //Receive the returned json array from server
                System.out.println(stringArray);
                //Parse json array to use properly in java
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(stringArray);

                for(int i = 0; i<jsonArray.size(); i++){
                    JSONArray jsonArray2 = (JSONArray) jsonArray.get(i); //Get each row from array
                    /*array[0] = friend's username
                    array[1] = notification count
                    array[2] = last message
                    array[3] = passed days since last chat*/
                    final String imageName = ServerFunctions.encodeURL(jsonArray2.get(0).toString()); //Get friend username to receive its photo from server

                    BufferedImage image;

                        try {
                            //Get profile photo and make it proper for JavaFx
                            image = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + imageName));
                            Image imagefx = SwingFXUtils.toFXImage(image, null);
                            int notifcount = Integer.parseInt(jsonArray2.get(1).toString());
                            //Create a new borderpane box for the friend with received information from server.
                            BorderPane friend = friendBox(imagefx,jsonArray2.get(0).toString(),jsonArray2.get(2).toString(),jsonArray2.get(1).toString(),jsonArray2.get(3).toString());
                            //Show the friend at the top of the list if there is a new or unread message. Add to next index if there is not.
                            if (notifcount > 0){
                                friendArray.add(0,friend); //Add friend to the list
                                friendsNameList.add(0,jsonArray2.get(0).toString()); //Add friend's name to the list to make search later.
                            } else{
                                friendArray.add(friendArray.size(),friend);
                                friendsNameList.add(jsonArray2.get(0).toString());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                //Get each friend from the list and add them to the friendsVBox to make them visible in the app
                for (Object element : friendArray) {
                    Platform.runLater(() -> friendsVBox.getChildren().add((Node) element));
                }
                checkNoResult(stringArray,noFriendLabel); //Check if the user has any friends, and if not, show noFriendLabel
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void getFriendRequests(){

        Thread thread = new Thread(() -> {
            try {
                String stringArray = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/getRequests.php", "");
                System.out.println(stringArray);
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(stringArray);
                Platform.runLater(() -> notificationVBox.getChildren().clear());
                //Get each requester's name from the server and create a request box with requestBox(requester name) function.
                //Add boxes to notificationVBox to show in the app.
                for(int i = 0; i<jsonArray.size(); i++){
                    int finalI = i;
                    Platform.runLater(() -> notificationVBox.getChildren().add(0,requestBox(jsonArray.get(finalI).toString())));
                }
                checkNoResult(stringArray,noNotifLabel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void sendMessage(){
        String msg = ServerFunctions.encodeURL(messageField.getText()); //Get typed message
        String curFriend = ServerFunctions.encodeURL(currentFriend); //Get current chatted friend
        listView.getItems().add(msg); //Add message to message list
        messageField.setText(""); //Clear the message field

        Thread thread = new Thread(() ->{
            try {
                //Send message to the server for the friend to see.
                String bmc = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/sendMessage.php","receiver=" + curFriend + "&message=" + msg);
                System.out.println(bmc);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        });
        thread.start();
        getMessages(); //Get and show all messages
        friendScrollPane.setVvalue(friendScrollPane.getHmin()); //Scroll down to last message
        updateFriendStatus(); //Update friend's box with new stats.
    }

    public static void getMessages(){
        ArrayList<String> msgList = new ArrayList<>(); //Create a message list
        String curFriend = ServerFunctions.encodeURL(currentFriend);

        Thread thread = new Thread(() -> {
            String stringArray;
            try {
                //Send request to server and receive an array of messages with the chatted friend.
                stringArray = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/getMessage.php", "receiver=" + curFriend);
                System.out.println(stringArray);
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(stringArray);
                for (int i = 0; i < jsonArray.size(); i++){
                    JSONArray msgArray = (JSONArray) jsonArray.get(i);
                    String user = msgArray.get(0).toString(); //Get the message owner
                    String message = msgArray.get(1).toString(); //Get the message
                    msgList.add(user + ": " + message); //Add them to the list as owner:message
                }
                Platform.runLater(() -> listView.getItems().clear());

                //Add each message form to listView
                for (int i = 0; i < msgList.size(); i++){
                    int finalI = i;
                    Platform.runLater(() -> listView.getItems().add(msgList.get(finalI)));
                }
                Platform.runLater(() -> listView.scrollTo(listView.getItems().size()-1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    //Add available languages into the app.
    public static void getLanguages(){
        languageChoiceBox.getItems().removeAll(languageChoiceBox.getItems());
        languageChoiceBox.getItems().addAll("Turkish-Türkçe", "English", "Norwegian-Norsk");
        languageChoiceBox.getSelectionModel().select("English");
    }

    public static void changeProfilePhoto(MouseEvent event){
            FileChooser fileChooser = new FileChooser();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            File file = fileChooser.showOpenDialog(stage); //Get the selected file

            if (file != null){
                try {
                    //Send selected file to the server
                    String string = ServerFunctions.FILERequest(ServerFunctions.serverURL + "/uploadPhoto.php", file, "photo");
                    System.out.println(string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }

    public static void searchFriend(KeyEvent event){
        friendsVBox.getChildren().clear(); //Remove all existing friends from view.
        String searchedFriend = searchFriendField.getText(); //Get typed characters on frontend.
        Thread thread = new Thread(() -> {
            //Search through all friends' names.
            for (int i = 0; i < friendsNameList.size(); i++) {
                //Check if friend's name includes typed characters.
                if (friendsNameList.get(i).contains(searchedFriend)) {
                    System.out.println("girdi: " + friendsNameList.get(i));
                    System.out.println(friendArray);
                    int finalI = i;
                    //Add searched friend to the emptied vbox.
                    Platform.runLater(() -> friendsVBox.getChildren().add(0, (Node) friendArray.get(finalI)));
                }
            }
        });
        thread.start();
    }

    public static void searchOnUsers(KeyEvent event){
        String writtenName = ServerFunctions.encodeURL(searchUserField.getText()); //Get typed characters on frontend.
        Thread thread = new Thread(() -> {
            try {
                //Send request to server with typed characters and receive a json array of possible users.
                String stringArray = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/getUsernames.php","username=" + writtenName);
                System.out.println(stringArray);
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(stringArray);
                Platform.runLater(() -> usersVBox.getChildren().clear());
                //Create box for each possible user and add it to list.
                for(int i = 0; i<jsonArray.size(); i++){
                    int finalI = i;
                    Platform.runLater(() -> usersVBox.getChildren().add(0,userBox(jsonArray.get(finalI).toString())));
                }
                checkNoResult(stringArray,noUserLabel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    //Creates a box in BorderPane form for friends with the parameters provided and returns it.
    public static BorderPane friendBox(Image friendPhoto, String friendName,String lastMessage,String notifCount,String lastDate){
        //Border style of the box
        String style = "-fx-border-color: #949494; -fx-border-width: 0.5px 0px 0.5px 0px";

        //Create the border pane
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(86);
        borderPane.setPrefWidth(237);
        borderPane.setStyle(style);
        borderPane.setCursor(Cursor.HAND);

        //Create a circle for the user's profile photo and locate in the border pane
        Circle friendProfilePhoto = new Circle();
        friendProfilePhoto.setRadius(21);
        friendProfilePhoto.setStrokeWidth(0);
        if (!friendPhoto.isError()) friendProfilePhoto.setFill(new ImagePattern(friendPhoto));

        BorderPane.setAlignment(friendProfilePhoto,Pos.CENTER);
        BorderPane.setMargin(friendProfilePhoto,new Insets(0,0,30,10));

        //Create and locate username label
        Label friend = new Label(friendName);
        friend.setPrefHeight(30);
        friend.setPrefWidth(246);
        friend.setTextFill(Color.WHITE);
        friend.setFont(new Font(15));
        friend.setPadding(new Insets(10,0,0,70));
        BorderPane.setAlignment(friend,Pos.CENTER_LEFT);

        //Create and locate last message label
        Label lstMsg = new Label(lastMessage);
        lstMsg.setMaxWidth(1.7976931348623157E308);
        lstMsg.setPrefHeight(18);
        lstMsg.setPrefWidth(49);
        lstMsg.setTextFill(Color.web("#949494"));
        BorderPane.setAlignment(lstMsg,Pos.CENTER_LEFT);
        BorderPane.setMargin(lstMsg,new Insets(0,0,10,15));

        //Create Hbox where notification count will be shown
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setMaxWidth(1.7976931348623157E308);
        hBox.setPrefHeight(42);
        hBox.setPrefWidth(87);
        hBox.setPadding(new Insets(0,0,10,0));
        BorderPane.setAlignment(hBox,Pos.CENTER_LEFT);

        //Circle for notification count
        Circle notifCircle = new Circle(9);
        notifCircle.setFill(Color.web("#ff6f00")); //Bakcground color of the circle
        notifCircle.setStroke(Color.web("#ff6f00")); //Border color of the circle
        Text text = new Text(notifCount); //Text to show notification count
        text.setFill(Color.WHITE);
        text.setBoundsType(TextBoundsType.VISUAL);
        StackPane stack = new StackPane();
        stack.getChildren().addAll(notifCircle, text);

        //Create and locate last chat date in the pane
        Label lstDt = new Label(lastDate);
        lstDt.setAlignment(Pos.CENTER);
        lstDt.setMaxWidth(1.7976931348623157E308);
        lstDt.setPrefHeight(18);
        lstDt.setPrefWidth(71);
        lstDt.setTextFill(Color.web("#949494"));
        BorderPane.setMargin(lstDt,new Insets(0,10,0,0));

        if (Integer.parseInt(notifCount) > 0){ hBox.getChildren().addAll(stack,lstDt); } //Show notification count if only it is more than 0
        else { hBox.getChildren().addAll(lstDt); }

        //Set locations of the properties in the pane
        borderPane.setLeft(friendProfilePhoto);
        borderPane.setTop(friend);
        borderPane.setCenter(lstMsg);
        borderPane.setRight(hBox);

        //Get friend's photo from server.
        String imageName = ServerFunctions.encodeURL(friendName);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image imagefx = SwingFXUtils.toFXImage(image, null);
        /*Create an action when border pane is clicked.
        Form a chat screen with the friend,
        Close the settings section if it is open,
        Make the chat screen visible
        Get the messages*/
        borderPane.setOnMouseClicked((event)->{
            getClickedFriend(imagefx,friendName, borderPane);
            settingsBorderPane.setVisible(false);
            chatBorderPane.setVisible(true);
            getMessages();
        });
        return borderPane;
    }

    //Creates a box in BorderPane form for friend requests with the requester name and returns it.
    public static BorderPane requestBox(String requesterName){
        String style = "-fx-background-color: #ff6f00";
        String style1 = "-fx-background-color: #ff5800";
        String style2 = "-fx-background-color: #1c1b1b";
        BorderPane requestPane = new BorderPane();
        requestPane.setPrefHeight(75);
        requestPane.setPrefWidth(237);
        requestPane.setStyle(style);

        //Circle for profile photo
        Circle circle = new Circle();
        circle.setRadius(21);
        circle.setFill(Color.DODGERBLUE);
        BorderPane.setAlignment(circle,Pos.CENTER);
        BorderPane.setMargin(circle,new Insets(10,0,0,10));

        //Label for requester's username
        Label senderName = new Label(requesterName);
        senderName.setPrefHeight(24);
        senderName.setPrefWidth(158);
        senderName.setFont(new Font(14));
        senderName.setPadding(new Insets(10,0,0,0));
        BorderPane.setAlignment(senderName,Pos.CENTER_LEFT);
        BorderPane.setMargin(senderName,new Insets(0,0,0,20));

        //HBox where accept and reject button will be located
        HBox hBox = new HBox();
        hBox.setPrefHeight(38);
        hBox.setPrefWidth(166);
        hBox.setSpacing(15);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(0,0,5,0));
        BorderPane.setMargin(hBox,new Insets(0,0,0,55));
        BorderPane.setAlignment(hBox,Pos.CENTER);

        //Accept button to accept the request
        Button acceptButton = new Button("Accept");
        acceptButton.setPrefHeight(26);
        acceptButton.setPrefWidth(65);
        acceptButton.setStyle(style1);
        acceptButton.setTextFill(Color.WHITE);
        acceptButton.setOnMouseClicked((event -> {
            try {
                String cevap = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/becomeFriend.php", "added=" + senderName.getText());
                System.out.println(cevap);
                if (cevap.equals("addfriend successful")){
                    warningMessage("Friend added!");
                } else if (cevap.equals("addfriend unsuccessful")){
                    warningMessage("Friend could not added!");
                }
                getFriendRequests();
                getFriends();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        //Reject button to deny the request and block the user.
        Button rejectButton = new Button("Reject");
        rejectButton.setPrefHeight(26);
        rejectButton.setPrefWidth(59);
        rejectButton.setStyle(style2);
        rejectButton.setTextFill(Color.WHITE);
        rejectButton.setOnMouseClicked((event -> {
            try {
                String cevap = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/rejectUser.php", "blockedUser=" + senderName.getText());
                System.out.println(cevap);
                if (cevap.equals("rejection successful")){
                    warningMessage("Request rejected!");
                } else if (cevap.equals("rejection unsuccessful")){
                    warningMessage("Request could not rejected!");
                }
                getFriendRequests();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        hBox.getChildren().addAll(acceptButton,rejectButton);
        //Locate properties in the pane
        requestPane.setLeft(circle);
        requestPane.setCenter(senderName);
        requestPane.setBottom(hBox);

        //Get the profile photo from server
        final String imageName = ServerFunctions.encodeURL(requesterName);
        Thread thread = new Thread(()-> {
            try {
                BufferedImage image1 = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + imageName));
                Image imagefx = SwingFXUtils.toFXImage(image1, null);
                if (!imagefx.isError()){
                    circle.setFill(new ImagePattern(imagefx));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return requestPane;
    }

    //Creates a box in HBox form for searched users and returns it.
    public static HBox userBox(String userName){ // creates a box that contains user's information when a user is searched.
        String backgroundStyle = "-fx-background-color:   #ff7d1a; -fx-border-color: #ff6f00; -fx-border-width: 1.5px";
        String buttonStyle = "-fx-background-color: #1c1b1b";

        //HBox where all properties will be gathered
        HBox hBox = new HBox();
        hBox.setPrefWidth(237);
        hBox.setPrefHeight(59);
        hBox.setStyle(backgroundStyle);
        hBox.setAlignment(Pos.CENTER_LEFT);

        Circle userPhoto = new Circle(21,Color.DODGERBLUE);
        HBox.setMargin(userPhoto,new Insets(0,0,0,10));

        Label username = new Label(userName);
        username.setPrefWidth(87);
        username.setPrefHeight(20);
        username.setMaxWidth(1.7976931348623157E308);
        username.setFont(new Font(14));
        username.setTextFill(Color.WHITE);
        username.setAlignment(Pos.CENTER);
        HBox.setHgrow(username, Priority.ALWAYS);
        HBox.setMargin(username,new Insets(0,10,0,10));

        //An 'Add' button to send friend request to the user.
        Button addButton = new Button("+ Add");
        addButton.setPrefHeight(26);
        addButton.setPrefWidth(51);
        addButton.setTextFill(Color.WHITE);
        addButton.setStyle(buttonStyle);
        HBox.setMargin(addButton,new Insets(0,10,0,0));
        addButton.setOnMouseClicked((event -> {
            String receiverUser = ServerFunctions.encodeURL(username.getText());
            try {
                String cevap = ServerFunctions.HTMLRequest(ServerFunctions.serverURL + "/sendFriendRequest.php", "receiverUser=" + receiverUser);
                System.out.println(cevap);
                if (cevap.equals("already friends")){
                    warningMessage("You're already friends!");
                } else if (cevap.equals("request sent")){
                    warningMessage("New request sent");
                } else if (cevap.equals("already sent")){
                    warningMessage("You already sent request!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        hBox.getChildren().addAll(userPhoto,username,addButton);

        final String imageName = ServerFunctions.encodeURL(username.getText());
            Thread thread = new Thread(()-> {
                try {
                    BufferedImage image = ImageIO.read(new URL(ServerFunctions.serverURL + "/getProfilePhoto.php?username=" + imageName));
                    Image imagefx = SwingFXUtils.toFXImage(image, null);
                    if (!imagefx.isError()){
                        userPhoto.setFill(new ImagePattern(imagefx));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        return hBox;
    }

    //Open or close a spesific section with animation.
    public static void openAndCloseSections(boolean value, VBox vBox){
        Stage currentStage = (Stage) Stage.getWindows().get(0); //Get current stage

        if(!value){
            Timeline timeline = new Timeline(); //Create timeline for animation.
            //Close all three sections.
            friendSection.setManaged(false);
            mailboxSection.setManaged(false);
            addFriendSection.setManaged(false);
            //Open only the spesificied section.
            vBox.setManaged(true); vBox.setVisible(true);
            vBox.translateYProperty().set(addFriendSection.getHeight());
            KeyValue kv  = new KeyValue(vBox.translateYProperty(),0, Interpolator.EASE_IN);
            KeyFrame kf = new KeyFrame(Duration.seconds(1),kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (vBox == addFriendSection){
                        currentStage.setTitle("Add Friend");
                        friendSection.setVisible(false);
                        mailboxSection.setVisible(false);
                    }
                    if (vBox == mailboxSection){
                        currentStage.setTitle("Mailbox");
                        addFriendSection.setVisible(false);
                        friendSection.setVisible(false);
                    }
                }
            });
            timeline.play();
        } else{
            //Close add friend section and mailbox section, open friend section.
            currentStage.setTitle("Chat");
            vBox.setVisible(false);
            vBox.setManaged(false);
            friendSection.setVisible(true);
            friendSection.setManaged(true);
        }
    }

    public static void logOff(MouseEvent event){
        try{
            //Load login scene with a new stage and hide the former one.
            FXMLLoader loader = new FXMLLoader(Function2.class.getResource("userinterfaces/login.fxml"));
            Parent loginPanel = loader.load();
            Scene scene = new Scene(loginPanel);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.hide();
            Stage newWindow = new Stage();
            newWindow.setScene(scene);
            newWindow.setResizable(false);
            newWindow.setFullScreen(false);
            newWindow.setTitle("Login");
            newWindow.show();

            //Clear stored username and password from settings.txt
            File file = new File(System.getProperty("user.home") + "/settings.txt");
            if(file.exists()){
                FileWriter writer = new FileWriter(file);
                writer.write("");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Load the contact panel
    public static void contactUs(MouseEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(Function2.class.getResource("userinterfaces/ContactPanel.fxml"));
            Parent contactPanel = loader.load();
            Scene scene = new Scene(contactPanel);
            Stage newWindow = new Stage();
            newWindow.setScene(scene);
            newWindow.setResizable(false);
            newWindow.setFullScreen(false);
            newWindow.setTitle("Contact");
            newWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Load warning window scene with a given text
    public static void warningMessage(String text){
        try {
            FXMLLoader loader = new FXMLLoader(Function2.class.getResource("userinterfaces/warningWindow.fxml"));
            Parent root = loader.load();
            WarningWindowController windowController = loader.getController();
            windowController.setLabelText(text);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setFullScreen(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

