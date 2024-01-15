import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class GarmentSearcher {

    // Create variables to store the images, file paths and app name for easier reference later in the code.
    private static final String filePath = "./inventory.txt";
    private static Inventory allGarments;
    private static final String appName = "Greek Geek's Garment Getter";
    private final static String iconPath = "./icon.png";
    private static final ImageIcon icon = new ImageIcon(iconPath);

    // Store the brand options into a Set.
    private static final Set<String> availableBrands = new HashSet<>();

    // The garment type that the user is interested in.
    public static GarmentType type;

    // Create Sets to determine which information is relevant to the garment type.
    private static final Set<Filter> hoodieFeatures = new LinkedHashSet<>(Arrays.asList(Filter.HOODIE_STYLE, Filter.POCKET_TYPE));
    private static final Set<Filter> tShirtFeatures = new LinkedHashSet<>(Arrays.asList(Filter.NECKLINE, Filter.SLEEVE_TYPE));

    // Create the main window view
    private static JFrame mainWindow = null;
    private static JPanel searchView = null;
    private static Geek geek = null;
    // Create a view for results
    private static JComboBox<String> optionsCombo = null;

    // Create variables for user information
    private static JTextField name;
    private static JTextField email;
    private static JTextField phoneNumber;
    private static JTextArea message;

    public static void main(String[] args) {
        allGarments = loadInventory(filePath);
        mainWindow = new JFrame(appName);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setIconImage(icon.getImage());
        mainWindow.setMinimumSize(new Dimension(300,300));
        searchView = generateFinalSearchView();
        mainWindow.setContentPane(searchView);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    /**
     * The purpose of this method is to create a new search view or refresh the search view to start a new search.
     * @return a new search view.
     */
    public static SearchView refreshSearchView(){
        return new SearchView(availableBrands);
    }

    /**
     * The purpose of this method is to create a search window that displays everything to the user.
     * @return a JPanel containing all the SearchView panels and a search button.
     */
    public static JPanel generateFinalSearchView(){
        // Crete an overall search window that contains everything the user needs to search for their garment.
        JPanel searchWindow = new JPanel();
        searchWindow.setLayout(new BorderLayout());

        // Create an empty search in the window.
        SearchView searchFilters = refreshSearchView();
        JPanel searchCriteriaPanel = searchFilters.generateSearchView();
        searchWindow.add(searchCriteriaPanel, BorderLayout.CENTER);

        // Create the search button that triggers the search based on the user's search criteria.
        JButton search = new JButton("SEARCH: The perfect outfit begins here");
        ActionListener actionListener = e -> conductSearch(searchFilters);
        search.addActionListener(actionListener);

        searchWindow.add(search,BorderLayout.SOUTH);
        searchWindow.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.WEST);
        searchWindow.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.EAST);

        return searchWindow;
    }

    /**
     * The purpose of this method is to take the user's search criteria and find relevant matches to display to them.
     * @param searchFilters takes a SearchView object that represents the user's search criteria.
     */
    public static void conductSearch(SearchView searchFilters){
        // Create a map to store the user's search criteria.
        Map<Filter,Object> filterMap = new HashMap<>();

        // Check if the garment type has been selected and add it to the search filters map.
        type = searchFilters.getGarmentType();
        if(type==GarmentType.SELECT_TYPE){
            JOptionPane.showMessageDialog(mainWindow, "Please select a garment type to continue.","Invalid Search",JOptionPane.INFORMATION_MESSAGE,icon);
            return;
        }
        filterMap.put(Filter.GARMENT_TYPE, type);

        // Add all other search criteria to the search filters map while checking for NA and empty entries.
        Material material = searchFilters.getMaterial();
        if(!material.equals(Material.NA)) filterMap.put(Filter.MATERIAL, material);
        Set<Size> chosenSizes = searchFilters.getSizes();
        if(!chosenSizes.isEmpty()){
            filterMap.put(Filter.SIZE,searchFilters.getSizes());
        } else {
            JOptionPane.showMessageDialog(mainWindow,"You must select at least one size to continue.", "Invalid Search",JOptionPane.INFORMATION_MESSAGE,icon);
            return;
        }
        Set<String> chosenBrands = searchFilters.getChosenBrands();
        if(!chosenBrands.isEmpty()) filterMap.put(Filter.BRAND,searchFilters.getChosenBrands());
        float minPrice = searchFilters.getMinPrice();
        float maxPrice = searchFilters.getMaxPrice();

        // Check which garment type the user is searching for and get their selections accordingly, while checking for NA entries.
        if(type.equals(GarmentType.HOODIE)){
            HoodieStyle hoodieStyle = searchFilters.getHoodieStyle();
            if(!hoodieStyle.equals(HoodieStyle.NA)) filterMap.put(Filter.HOODIE_STYLE, hoodieStyle);
            PocketType pocketType = searchFilters.getPocketType();
            if(!pocketType.equals(PocketType.NA)) filterMap.put(Filter.POCKET_TYPE, pocketType);
        }
        if(type.equals(GarmentType.T_SHIRT)){
            Neckline neckline = searchFilters.getNeckline();
            if(!neckline.equals(Neckline.NA)) filterMap.put(Filter.NECKLINE, neckline);
            SleeveType sleeveType = searchFilters.getSleeveType();
            if(!sleeveType.equals(SleeveType.NA)) filterMap.put(Filter.SLEEVE_TYPE, sleeveType);
        }

        // Look for matching garments in the inventory and display them to the user.
        GarmentSpecs garmentSpecs = new GarmentSpecs(filterMap, minPrice, maxPrice);
        List<Garment> relevantGarments = allGarments.findMatch(garmentSpecs);
        showResults(relevantGarments);
    }

    /**
     * The purpose of this method is to display the search results to the user. Including a description of the product and a drop-down list to select from.
     * @param relevantGarments a list of garments that fit the user's search criteria.
     */
    public static void showResults(List<Garment> relevantGarments){
        // Display a message to the user if there are no relevant results.
        if(relevantGarments.size()==0){
            noResults();
            return;
        }
        // Otherwise, display the search results.
        JPanel results = new JPanel();
        results.setLayout(new BorderLayout());
        results.add(Box.createRigidArea(new Dimension(0,10)),BorderLayout.NORTH);
        results.add(generateGarmentDescriptions(relevantGarments),BorderLayout.CENTER);
        results.add(selectFromResultsPanel(relevantGarments),BorderLayout.SOUTH);
        results.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.WEST);
        results.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.EAST);
        mainWindow.setContentPane(results);
        mainWindow.revalidate();
    }

    /**
     * The purpose of this method is to create a scroll panel that contains all the garment descriptors relevant to the user's search.
     * @param relevantGarments a list of garments that match the user's search criteria.
     * @return a scroll panel containing the descriptions of the garments that match the user's search criteria.
     */
    public static JScrollPane generateGarmentDescriptions(List<Garment> relevantGarments){
        // Create a list of options for the user to select from in a drop-down box.
        String[] options = new String[relevantGarments.size()];

        // Create a panel for the descriptions.
        JPanel garmentDescriptions = new JPanel();
        garmentDescriptions.setBorder(BorderFactory.createTitledBorder("AWESOME! These items match your search: "));
        garmentDescriptions.setLayout(new BoxLayout(garmentDescriptions,BoxLayout.Y_AXIS));
        garmentDescriptions.add(Box.createRigidArea(new Dimension(0,10)));

        //loop through the garments, generating a description and adding it to the description panel.
        for (int i = 0; i < relevantGarments.size(); i++) {
            JTextArea garmentDescription = new JTextArea();
            if (type.equals(GarmentType.HOODIE)) garmentDescription = new JTextArea(relevantGarments.get(i).getGarmentInformation(hoodieFeatures));
            if (type.equals(GarmentType.T_SHIRT)) garmentDescription = new JTextArea(relevantGarments.get(i).getGarmentInformation(tShirtFeatures));
            garmentDescription.setEditable(false);
            garmentDescription.setLineWrap(true);
            garmentDescription.setWrapStyleWord(true);
            garmentDescriptions.add(garmentDescription);

            // Add the garment names to the list for the drop-down box.
            options[i] = relevantGarments.get(i).getName();
        }
        // Create the drop-down list.
        optionsCombo = new JComboBox<>(options);

        // Add a scroll bar
        JScrollPane verticalScrollBar = new JScrollPane(garmentDescriptions);
        verticalScrollBar.setPreferredSize(new Dimension(300, 450));
        verticalScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        SwingUtilities.invokeLater(() -> verticalScrollBar.getViewport().setViewPosition( new Point(0, 0) ));

        return verticalScrollBar;
    }

    /**
     * The purpose of this method is to create the drop-down list panel where the user can select a garment to buy.
     * @param relevantGarments a list of Garment objects that are relevant to the user's search criteria.
     * @return a JPanel that contains a drop-down list where the user can elect to make a purchase.
     */
    public static JPanel selectFromResultsPanel(List<Garment> relevantGarments){
        // Allow the user to start a new search if they don't like their search results.
        JLabel noneMessage = new JLabel("If you don't like these items, close to exit, or search again with different criteria");
        JButton editSearchCriteriaButton = new JButton("Search again");
        ActionListener actionListenerEditCriteria = e -> reGenerateSearchView();
        editSearchCriteriaButton.addActionListener(actionListenerEditCriteria);

        // The user can also select from one of the search result options.
        String defaultOption = "Select your favourite item";
        optionsCombo.addItem(defaultOption);
        optionsCombo.setSelectedItem(defaultOption);
        ActionListener actionListener = e -> checkUserSelection(relevantGarments);
        optionsCombo.addActionListener(actionListener);

        // Create a JPanel for the button and the drop-down list
        JPanel buttonOptionPanel = new JPanel();
        buttonOptionPanel.add(optionsCombo);
        buttonOptionPanel.add(editSearchCriteriaButton);

        // Create JPanel that displays everything to the user
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel,BoxLayout.Y_AXIS));
        selectionPanel.add(Box.createRigidArea(new Dimension(0,10)));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Please select which garment you would like to buy:"));
        selectionPanel.add(noneMessage);
        selectionPanel.add(buttonOptionPanel);
        selectionPanel.add(Box.createRigidArea(new Dimension(10,0)));

        return selectionPanel;
    }

    /**
     * The purpose of this method is to get the Garment information of the selected garment and have the user place their order.
     * @param relevantGarments a list of garments relevant to the user's search criteria.
     */
    public static void checkUserSelection(List<Garment> relevantGarments){
        String decision = (String) optionsCombo.getSelectedItem();
        assert decision != null;

        for (Garment g : relevantGarments) {
            if (decision.equals(g.getName())) {
                int answer = JOptionPane.showConfirmDialog(mainWindow, "Would you like to place an order for " + g.getName() + "?");
                if(answer == JOptionPane.YES_OPTION) contactForm(g);
                if(answer == JOptionPane.NO_OPTION) reGenerateSearchView();
                if(answer == JOptionPane.CANCEL_OPTION) showResults(relevantGarments);
                break;
            }
        }

    }

    /**
     *
     *
     * @return a JPanel for the user to enter their information
     */
    public static JPanel contactForm(Garment chosenGarment){
        // Have the user fill out the order form.
        JLabel orderMessage = new JLabel("To place an order for "+chosenGarment.getName()+" fill in the form below");
        orderMessage.setAlignmentX(0);

        // Create a space for the user to enter their information and special requests.
        JLabel enterName = new JLabel("Full name");
        name = new JTextField(12);

        JLabel enterEmail = new JLabel("Email address");
        email = new JTextField(12);

        JLabel enterPhoneNumber = new JLabel("Phone number");
        phoneNumber = new JTextField(10);

        JLabel enterMessage = new JLabel("Type your query below");
        message = new JTextArea(6,12);

        JScrollPane jScrollPane = new JScrollPane(message);
        jScrollPane.getViewport().setPreferredSize(new Dimension(250,100));

        JButton detailsCorrect = new JButton("submit my details");
        ActionListener actionListenerDetailsCorrect = e -> validateInput(chosenGarment);
        detailsCorrect.addActionListener(actionListenerDetailsCorrect);

        // Create a panel to display everything to the user
        JPanel userInputPanel = new JPanel();
        userInputPanel.setLayout(new BoxLayout(userInputPanel,BoxLayout.Y_AXIS));
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));
        userInputPanel.setAlignmentX(0);

        enterName.setAlignmentX(0);
        name.setAlignmentX(0);
        userInputPanel.add(enterName);
        userInputPanel.add(name);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));

        enterEmail.setAlignmentX(0);
        email.setAlignmentX(0);
        userInputPanel.add(enterEmail);
        userInputPanel.add(email);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));

        enterPhoneNumber.setAlignmentX(0);
        phoneNumber.setAlignmentX(0);
        userInputPanel.add(enterPhoneNumber);
        userInputPanel.add(phoneNumber);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));

        enterMessage.setAlignmentX(0);
        message.setAlignmentX(0);
        userInputPanel.add(enterMessage);
        jScrollPane.setAlignmentX(0);
        userInputPanel.add(jScrollPane);
        userInputPanel.add(Box.createRigidArea(new Dimension(0,10)));

       detailsCorrect.setAlignmentX(0);
       userInputPanel.add(detailsCorrect);

        JPanel mainFramePanel = new JPanel();
        mainFramePanel.setLayout(new BorderLayout());
        mainFramePanel.add(userInputPanel,BorderLayout.CENTER);
        mainFramePanel.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.WEST);
        mainFramePanel.add(Box.createRigidArea(new Dimension(20,0)),BorderLayout.EAST);

        mainWindow.setContentPane(mainFramePanel);
        mainWindow.revalidate();

        return userInputPanel;
    }

    public static void validateInput(Garment chosenGarment){
        String geekName;
        String geekEmail;
        long geekPhoneNumber;
        String geekMessage;

        // Check the entries and create a new Geek object
        if(!(name.getText().length() > 0)) {
            JOptionPane.showMessageDialog(mainWindow, "Please enter your name", "Invalid Name Entry",JOptionPane.QUESTION_MESSAGE, icon);
            return;
        }
        geekName = name.getText();

        if(!email.getText().contains("@")) {
            JOptionPane.showMessageDialog(mainWindow, "Please enter a valid email address containing an @ symbol", "Invalid Email Address",JOptionPane.QUESTION_MESSAGE, icon);
            return;
        }
        geekEmail = email.getText();

        if(phoneNumber.getText().length() != 10){
            JOptionPane.showMessageDialog(mainWindow, "Please enter a valid 10 digit phone number", "Invalid Phone Number",JOptionPane.QUESTION_MESSAGE, icon);
            return;
        }
        try{
            geekPhoneNumber = Long.parseLong(phoneNumber.getText().strip());
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(mainWindow, "Please enter a valid 10 digit phone number", "Invalid Phone Number",JOptionPane.QUESTION_MESSAGE, icon);
            return;
        }

        geekMessage = message.getText();

        geek = new Geek(geekName, geekPhoneNumber, geekEmail, geekMessage);

        confirmOrder(chosenGarment);
    }

    public static void confirmOrder(Garment chosenGarment){
        int answer = JOptionPane.showConfirmDialog(mainWindow,"Your details have been saved.\n Would you like to submit the order now?");
        if(answer == JOptionPane.YES_OPTION) submitOrder(geek, chosenGarment);
        if(answer == JOptionPane.NO_OPTION) reGenerateSearchView();
        if(answer == JOptionPane.CANCEL_OPTION) return;
        reGenerateSearchView();
    }
    /**
     * The purpose of this method is to create a fresh search view for the user.
     */
    public static void reGenerateSearchView(){
        searchView = generateFinalSearchView();
        mainWindow.setContentPane(searchView);
        mainWindow.revalidate();
    }

    /**
     * The purpose of this method is to display a message to the user notifying them that their search has returned no results.
     * The user will then be able to complete a new search with a fresh search view.
     */
    public static void noResults(){
        JOptionPane.showMessageDialog(mainWindow,"Sorry, we don't have any garments that match your search criteria","Your search returned no results",JOptionPane.INFORMATION_MESSAGE,icon);
        reGenerateSearchView();
    }

// Code no longer required.
//    public static GarmentSpecs getFilters(){
//        Map<Filter,Object> filterMap = new HashMap<>();
//        GarmentType garmentType = (GarmentType) JOptionPane.showInputDialog(null,"What kind of garment are you looking for?",appName, JOptionPane.QUESTION_MESSAGE,null,GarmentType.values(),GarmentType.HOODIE);
//        if(garmentType==null)System.exit(0);
//        filterMap.put(Filter.GARMENT_TYPE,garmentType);
//
//        Material material = (Material) JOptionPane.showInputDialog(null,"Please select your preferred material:",appName, JOptionPane.QUESTION_MESSAGE,null,Material.values(),Material.COTTON);
//        if(material==null)System.exit(0);
//        if(!material.equals(Material.NA)) filterMap.put(Filter.MATERIAL,material);
//
//        Size size = (Size) JOptionPane.showInputDialog(null,"Please select your preferred size (XS - 4XL):",appName, JOptionPane.QUESTION_MESSAGE,null,Size.values(),Size.M);
//        if(size==null)System.exit(0);
//        filterMap.put(Filter.SIZE,size);
//
//        String brand = (String) JOptionPane.showInputDialog(null,"Please select your preferred brand:",appName, JOptionPane.QUESTION_MESSAGE,null,allGarments.getAllBrands().toArray(), allGarments.getAllBrands().toArray()[1]);
//        if(brand==null)System.exit(0);
//        if(!brand.equals("NA")) filterMap.put(Filter.BRAND,brand);
//
//        if(garmentType.equals(GarmentType.T_SHIRT)) {
//            Neckline neckline = (Neckline) JOptionPane.showInputDialog(null, "Please select your preferred neckline style:", appName, JOptionPane.QUESTION_MESSAGE, null, Neckline.values(), Neckline.CREW);
//            if (neckline == null) System.exit(0);
//
//            SleeveType sleeveType = (SleeveType) JOptionPane.showInputDialog(null, "Please select your preferred sleeve type:", appName, JOptionPane.QUESTION_MESSAGE, null, SleeveType.values(), SleeveType.SHORT);
//            if (sleeveType == null) System.exit(0);
//
//            if(!neckline.equals(Neckline.NA)) filterMap.put(Filter.NECKLINE,neckline);
//            if(!sleeveType.equals(SleeveType.NA)) filterMap.put(Filter.SLEEVE_TYPE,sleeveType);
//        }
//        else {
//            HoodieStyle hoodieStyle = (HoodieStyle) JOptionPane.showInputDialog(null, "Please select your preferred hoodie style:", appName, JOptionPane.QUESTION_MESSAGE, null, HoodieStyle.values(), HoodieStyle.PULLOVER);
//            if (hoodieStyle == null) System.exit(0);
//
//            PocketType pocketType = (PocketType) JOptionPane.showInputDialog(null, "Please select your preferred pocket type:", appName, JOptionPane.QUESTION_MESSAGE, null, PocketType.values(), PocketType.KANGAROO);
//            if (pocketType == null) System.exit(0);
//
//            if(!hoodieStyle.equals(HoodieStyle.NA)) filterMap.put(Filter.HOODIE_STYLE,hoodieStyle);
//            if(!pocketType.equals(PocketType.NA)) filterMap.put(Filter.POCKET_TYPE,pocketType);
//        }
//
//        int minPrice=-1,maxPrice = -1;
//        while(minPrice<0) {
//            String userInput = JOptionPane.showInputDialog(null, "Please enter the lowest price", appName, JOptionPane.QUESTION_MESSAGE);
//            if(userInput==null)System.exit(0);
//            try {
//                minPrice = Integer.parseInt(userInput);
//                if(minPrice<0) JOptionPane.showMessageDialog(null,"Price must be >= 0.",appName, JOptionPane.ERROR_MESSAGE);
//            }
//            catch (NumberFormatException e){
//                JOptionPane.showMessageDialog(null,"Invalid input. Please try again.", appName, JOptionPane.ERROR_MESSAGE);
//            }
//        }
//        while(maxPrice<minPrice) {
//            String userInput = JOptionPane.showInputDialog(null, "Please enter the highest price", appName, JOptionPane.QUESTION_MESSAGE);
//            if(userInput==null)System.exit(0);
//            try {
//                maxPrice = Integer.parseInt(userInput);
//                if(maxPrice<minPrice) JOptionPane.showMessageDialog(null,"Price must be >= "+minPrice,appName, JOptionPane.ERROR_MESSAGE);
//            }
//            catch (NumberFormatException e){
//                JOptionPane.showMessageDialog(null,"Invalid input. Please try again.", appName, JOptionPane.ERROR_MESSAGE);
//            }
//        }
//        return new GarmentSpecs(filterMap,minPrice,maxPrice);
//    }

//    public static void processSearchResults(GarmentSpecs dreamGarment){
//        List<Garment> matchingGarments = allGarments.findMatch(dreamGarment);
//        if(matchingGarments.size()>0) {
//            Map<String, Garment> options = new HashMap<>();
//            StringBuilder infoToShow = new StringBuilder("Matches found!! The following garments meet your criteria: \n");
//            for (Garment matchingGarment : matchingGarments) {
//                infoToShow.append(matchingGarment.getGarmentInformation());
//                options.put(matchingGarment.getName(), matchingGarment);
//            }
//            String choice = (String) JOptionPane.showInputDialog(null, infoToShow + "\n\nPlease select which t-shirt you'd like to order:", appName, JOptionPane.INFORMATION_MESSAGE, null, options.keySet().toArray(), "");
//            if(choice==null) System.exit(0);
//            Garment chosenGarment = options.get(choice);
//            submitOrder(getUserContactInfo(),chosenGarment, (Size) dreamGarment.getFilter(Filter.SIZE));
//            JOptionPane.showMessageDialog(null,"Thank you! Your order has been submitted. "+
//                    "One of our friendly staff will be in touch shortly.",appName, JOptionPane.INFORMATION_MESSAGE);
//        }
//        else{
//            JOptionPane.showMessageDialog(null,"Unfortunately none of our garments meet your criteria :("+
//                    "\n\tTo exit, click OK.",appName, JOptionPane.INFORMATION_MESSAGE);
//        }
//    }
    
//    public static Geek getUserContactInfo(){
//        String name = JOptionPane.showInputDialog(null,"Please enter your full name.",appName, JOptionPane.QUESTION_MESSAGE);
//        if(name==null) System.exit(0);
//        long phoneNumber=0;
//        while(phoneNumber==0) {
//            try {
//                String userInput = JOptionPane.showInputDialog(null, "Please enter your phone number.", appName, JOptionPane.QUESTION_MESSAGE);
//                if(userInput==null) System.exit(0);
//                phoneNumber = Long.parseLong(userInput);
//            } catch (NumberFormatException e) {
//                phoneNumber = Long.parseLong(JOptionPane.showInputDialog(null, "Invalid entry. Please enter your phone number.", appName, JOptionPane.ERROR_MESSAGE));
//            }
//        }
//        return new Geek(name,phoneNumber);
//    }

    public static void submitOrder(Geek geek, Garment Garment) {
        String filePath = geek.getName().replace(" ","_")+"_"+Garment.getProductCode()+".txt";
        Path path = Path.of(filePath);
        String lineToWrite = "Order details:\n\t" +
                "Name: "+geek.getName()+
                "\n\tPhone number: 0"+geek.getPhoneNumber()+
                "\n\tEmail Address: "+geek.getEmailAddress()+
                "\n\tItem: "+Garment.getName()+" ("+Garment.getProductCode()+")"+
                //"\n\tSize: "+size;
                "\n\n\tMessage: "+geek.getMessage();
        try {
            Files.writeString(path, lineToWrite);
            JOptionPane.showMessageDialog(mainWindow,"Thank you for your order.\nOne of our friendly staff will be in touch shortly.", "Order Placed", JOptionPane.INFORMATION_MESSAGE,icon);
        }catch (IOException io){
            System.out.println("Order could not be placed. \nError message: "+io.getMessage());
            System.exit(0);
        }
    }

    public static Inventory loadInventory(String filePath) {
        Inventory allGarments = new Inventory();
        Path path = Path.of(filePath);
        List<String> fileContents = null;
        try {
            fileContents = Files.readAllLines(path);
        }catch (IOException io){
            System.out.println("File could not be found");
            System.exit(0);
        }
        for(int i=1;i<fileContents.size();i++){
            String[] info = fileContents.get(i).split("\\[");
            String[] singularInfo = info[0].split(",");
            String sizesRaw = info[1].replace("]","");
            String description = info[2].replace("]","");

            GarmentType garmentType = null;
            try {
                garmentType = GarmentType.valueOf(singularInfo[0].replace("-","_").toUpperCase()); //error catching
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. type data could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            String name = singularInfo[1];

            long productCode = 0;
            try{
                productCode = Long.parseLong(singularInfo[2]);
            }catch (NumberFormatException n) {
                System.out.println("Error in file. Product code could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            double price = 0;
            try{
                price = Double.parseDouble(singularInfo[3]);
            }catch (NumberFormatException n){
                System.out.println("Error in file. Price could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+n.getMessage());
                System.exit(0);
            }

            String brand = singularInfo[4];
            availableBrands.add(brand);

            Material material = null;
            try{
                material = Material.valueOf(singularInfo[5].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Material data could not be parsed for garment on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            Neckline neckline = null;
            try{
                neckline = Neckline.valueOf(singularInfo[6].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Neckline data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            SleeveType sleeveType = null;
            try{
                sleeveType = SleeveType.valueOf(singularInfo[7].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Sleeve type data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            PocketType pocketType = null;
            try{
                pocketType = PocketType.valueOf(singularInfo[8].toUpperCase());
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Pocket type data could not be parsed for hoodie on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }
            HoodieStyle hoodieStyle = null;
            try{
                hoodieStyle = HoodieStyle.valueOf(singularInfo[9].toUpperCase().replace(" ","_"));
            }catch (IllegalArgumentException e){
                System.out.println("Error in file. Style data could not be parsed for hoodie on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                System.exit(0);
            }

            Set<Size> sizes = new HashSet<>();
            for(String s: sizesRaw.split(",")){
                Size size = Size.S;
                try {
                    size = Size.valueOf(s);
                }catch (IllegalArgumentException e){
                    System.out.println("Error in file. Size data could not be parsed for t-shirt on line "+(i+1)+". Terminating. \nError message: "+e.getMessage());
                    System.exit(0);
                }
                sizes.add(size);
            }


            Map<Filter,Object> filterMap = new LinkedHashMap<>();
            filterMap.put(Filter.GARMENT_TYPE,garmentType);
            filterMap.put(Filter.BRAND,availableBrands);
            filterMap.put(Filter.MATERIAL,material);
            filterMap.put(Filter.SIZE,sizes);
            if(!neckline.equals(Neckline.NA)) filterMap.put(Filter.NECKLINE,neckline);
            if(!sleeveType.equals(SleeveType.NA)) filterMap.put(Filter.SLEEVE_TYPE,sleeveType);
            if(!hoodieStyle.equals(HoodieStyle.NA)) filterMap.put(Filter.HOODIE_STYLE,hoodieStyle);
            if(!pocketType.equals(PocketType.NA)) filterMap.put(Filter.POCKET_TYPE,pocketType);

            GarmentSpecs dreamGarment = new GarmentSpecs(filterMap);

            Garment Garment = new Garment(name,productCode,price,description,dreamGarment);
            allGarments.addGarment(Garment);
        }
        return allGarments;
    }
}
