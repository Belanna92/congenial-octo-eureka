import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.Set;

public class SearchView {

    // Create a variable to store the type of garment the user is interested in.
    private GarmentType garmentType = GarmentType.SELECT_TYPE;

    // Create a variable to store the card layout.
    private final CardLayout cardLayout = new CardLayout();

    // Create string variables to store the names of each card layout
    private final String IMAGE_PANEL = "T-Shirt Images";
    private final String T_SHIRT_PANEL = "T-Shirt";
    private final String HOODIE_PANEL = "Hoodie";

    // Create a JPanel for the above cards
    private JPanel garmentTypePanel;

    // Create variables to store the minimum and maximum price the user is willing to spend.
    private final int defaultMinPrice = 0;
    private final int defaultMaxPrice = 100;
    private float minPrice = defaultMinPrice;
    private float maxPrice = defaultMaxPrice;
    // Create JLabels that will display a message to the user during data validation.
    private final JLabel feedbackMin = new JLabel("");
    private final JLabel feedbackMax = new JLabel("");

    // Available brands from the inventory.
    private final Set<String> availableBrands;

    // Create variables to store the users choices.
    private Set<Size> sizes;
    private Set<String> chosenBrands;
    private Material material;
    private HoodieStyle hoodieStyle;
    private PocketType pocketType;
    private Neckline neckline;
    private SleeveType sleeveType;

    public SearchView(Set<String> availableBrands) {
        this.availableBrands = availableBrands;
        this.chosenBrands = new HashSet<>();
        this.sizes = new HashSet<>();
    }

    // MAIN SEARCH VIEW
    /**
     * The purpose of this method is to display the search view to the user.
     * @return a JPanel that contains all the relevant search criteria panels.
     */
    public JPanel generateSearchView(){
        // Create an overall panel that contains the search view with all panels.
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));

        // The user will always be asked to select a garment type.
        JPanel type = this.userInputGarmentType();
        type.setAlignmentX(0);
        filters.add(type);

        // The user will always be displayed this information.
        JPanel alwaysDisplayed = this.userInputAlwaysDisplayed();
        alwaysDisplayed.setAlignmentX(0);
        filters.add(alwaysDisplayed);

        filters.add(Box.createRigidArea(new Dimension(0,20)));

        // Garment specific cardLayout
        garmentTypePanel = new JPanel();
        garmentTypePanel.setAlignmentX(0);
        garmentTypePanel.setLayout(cardLayout);
        garmentTypePanel.add(this.imagePanel(),IMAGE_PANEL);
        garmentTypePanel.add(this.userInputHoodie(),HOODIE_PANEL);
        garmentTypePanel.add(this.userInputTShirt(), T_SHIRT_PANEL);
        filters.add(garmentTypePanel);

        return filters;
    }

    // INNER PANELS
    /**
     * The purpose of this method is to have the user select the type of garment that they are interested in.
     * This information will be available in a drop-down list for the user to select from.
     * @return a JPanel displaying the type of garments in a drop-down list and a prompt for the user.
     */
    public JPanel userInputGarmentType(){
        // Drop down list of the available garment types for the user to select.
        JComboBox<GarmentType> garmentTypeJComboBox = new JComboBox<>(GarmentType.values());
        // Have the suer select the type of garment before anything else
        garmentTypeJComboBox.requestFocusInWindow();
        // Give the user an instruction on what they are expected to do, by setting th default selection to "SELECT_TYPE"
        garmentTypeJComboBox.setSelectedItem(GarmentType.SELECT_TYPE);

        // Add an item listener to update recognise when the user has made a selection, and respond accordingly.
        garmentTypeJComboBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) ifTypeSelected(garmentTypeJComboBox);
        });
        // Create the JPanel that displays everything to the user.
        JPanel garmentTypeSelectionPanel = new JPanel();
        garmentTypeSelectionPanel.setLayout(new BoxLayout(garmentTypeSelectionPanel, BoxLayout.Y_AXIS));
        garmentTypeSelectionPanel.add(Box.createRigidArea(new Dimension(0,20)));
        garmentTypeSelectionPanel.add(garmentTypeJComboBox);
        garmentTypeSelectionPanel.add(Box.createRigidArea(new Dimension(0,20)));
        return garmentTypeSelectionPanel;
    }

    /**
     * The purpose of this method is to update the garmentType that the user has selected and then display the appropriate card layout.
     * @param garmentTypeJComboBox is the dropdown list where the user selected the type of garment that they are interested in.
     */
    public void ifTypeSelected(JComboBox<GarmentType> garmentTypeJComboBox){
        // Update the garmentType variable created earlier.
        garmentType = (GarmentType) garmentTypeJComboBox.getSelectedItem();
        // Do not need to handle a null pointer exception because the selection cannot be blank.
        assert garmentType != null;

        // Change the card to display the appropriate section based off the user's selection.
        if(garmentType.equals(GarmentType.SELECT_TYPE)) cardLayout.show(garmentTypePanel, IMAGE_PANEL);
        else if(garmentType.equals(GarmentType.T_SHIRT)) cardLayout.show(garmentTypePanel, T_SHIRT_PANEL);
        else if(garmentType.equals(GarmentType.HOODIE)) cardLayout.show(garmentTypePanel, HOODIE_PANEL);
    }

    /**
     * The purpose of this method is to create and return a panel of buttons for the user to select from the available garment materials.
     * @return a JPanel containing a button group of available garment materials.
     */
    public JPanel userInputMaterial(){
        // The user will need to select one type of fabric from the available options or select NA.
        ButtonGroup materialButtonGroup = new ButtonGroup();

        // Create buttons for the button group
        JRadioButton cotton = new JRadioButton(Material.COTTON.toString());
        JRadioButton polyester = new JRadioButton(Material.POLYESTER.toString());
        JRadioButton woolBlend = new JRadioButton(Material.WOOL_BLEND.toString());
        JRadioButton na = new JRadioButton(Material.NA.toString(), true);
        cotton.requestFocusInWindow();
        // Material will default to NA
        material = Material.NA;

        // Add buttons to button group
        materialButtonGroup.add(cotton);
        materialButtonGroup.add(polyester);
        materialButtonGroup.add(woolBlend);
        materialButtonGroup.add(na);

        // Add the value when the user selects one of the buttons.
        cotton.setActionCommand(Material.COTTON.name());
        polyester.setActionCommand(Material.POLYESTER.name());
        woolBlend.setActionCommand(Material.WOOL_BLEND.name());
        na.setActionCommand(Material.NA.name());

        // Update the material variable when the user makes a selection.
        ActionListener actionListener = e-> material = Material.valueOf(materialButtonGroup.getSelection().getActionCommand().toUpperCase());
        cotton.addActionListener(actionListener);
        polyester.addActionListener(actionListener);
        woolBlend.addActionListener(actionListener);
        na.addActionListener(actionListener);

        // Create the JPanel that displays everything to the user.
        JPanel materialPanel = new JPanel();
        materialPanel.setAlignmentX(0);
        materialPanel.setBorder(BorderFactory.createTitledBorder("Please select your preferred fabric."));
        materialPanel.add(cotton);
        materialPanel.add(polyester);
        materialPanel.add(woolBlend);
        materialPanel.add(na);

        return materialPanel;
    }

    /**
     * The purpose of this method is to create and return a panel with a list for the user to select the available sizes.
     * @return a JPanel containing a list of available garment sizes. Multiple sizes can be selected.
     */
    public JPanel userInputSize(){
        // The user will be able to select multiple size options for their search.
        JList<Size> selectSizes = new JList<>(Size.values());
        selectSizes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Create a scroll panel
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(selectSizes);
        selectSizes.setLayoutOrientation(JList.VERTICAL);
        scrollPane.setPreferredSize(new Dimension(250, 60));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Start the scroll bar at the top of the list.
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition( new Point(0, 0) ));
        
        // Update the sizes variable when the user selects a size.
        ListSelectionListener listSelectionListener = e -> sizes = new HashSet<>(selectSizes.getSelectedValuesList());
        selectSizes.addListSelectionListener(listSelectionListener);

        // Create the JPanel that displays everything to the user.
        JPanel sizesPanel = new JPanel();
        sizesPanel.setLayout(new BoxLayout(sizesPanel, BoxLayout.Y_AXIS));
        sizesPanel.add(Box.createRigidArea(new Dimension(0,5)));
        JLabel instruction = new JLabel("Please select your preferred size(s)");
        instruction.setAlignmentX(0);
        sizesPanel.add(instruction);
        JLabel clarification = new JLabel("(To multi-select, hold Ctrl)");
        clarification.setAlignmentX(0);
        clarification.setFont(new Font("", Font. ITALIC, 12));
        sizesPanel.add(clarification);
        scrollPane.setAlignmentX(0);
        sizesPanel.add(scrollPane);
        sizesPanel.add(Box.createRigidArea(new Dimension(0,5)));

        return sizesPanel;
    }

    /**
     * The purpose of this method is to return a panel that takes the users min and max price preferences as input.
     * @return a JPanel that provides the user with text boxes to enter their preferred min and max price to spend on a garment.
     */
    public JPanel userInputPriceRange(){
        // The user will enter their preferred min and max price in text boxes.
        JLabel minLabel = new JLabel("Minimum price");
        JLabel maxLabel = new JLabel("Maximum price");
        JTextField min = new JTextField(4);
        JTextField max = new JTextField(4);

        // Default values for the text boxes
        min.setText(String.valueOf(defaultMinPrice));
        max.setText(String.valueOf(defaultMaxPrice));

        // Update the feedback labels colours so that they stand out.
        feedbackMin.setFont(new Font("", Font. ITALIC, 12));
        feedbackMin.setForeground(Color.RED);
        feedbackMax.setFont(new Font("", Font. ITALIC, 12));
        feedbackMax.setForeground(Color.RED);

        // Check that the input is valid and then update the minPrice variable.
        min.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(!checkMin(min)) min.requestFocus();
                checkMax(max);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if(!checkMin(min))min.requestFocus();
                checkMax(max);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        // Check that the input is valid and then update the maxPrice variable.
        max.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(!checkMax(max)) max.requestFocusInWindow();
                checkMin(min);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if(!checkMax(max))max.requestFocusInWindow();
                checkMin(min);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        // Create the JPanels that display everything to the user.
        JPanel priceRangePanel = new JPanel();
        priceRangePanel.add(minLabel);
        priceRangePanel.add(min);
        priceRangePanel.add(maxLabel);
        priceRangePanel.add(max);

        JPanel outerPricePanel = new JPanel();
        outerPricePanel.setBorder(BorderFactory.createTitledBorder("Please enter you preferred price range."));
        outerPricePanel.setLayout(new BoxLayout(outerPricePanel,BoxLayout.Y_AXIS));
        outerPricePanel.setAlignmentX(0);
        outerPricePanel.add(priceRangePanel);
        feedbackMin.setAlignmentX(0);
        feedbackMax.setAlignmentX(0);
        outerPricePanel.add(feedbackMin);
        outerPricePanel.add(feedbackMax);

        return outerPricePanel;

    }

    /**
     * The purpose of this method is to validate the user's input for min price.
     * @param minEntry the JTextField used to enter min price.
     * @return true if valid price, false if invalid.
     */
    private boolean checkMin(JTextField minEntry){
        feedbackMin.setText("");
        try{
            float tempMin = Float.parseFloat(minEntry.getText());
            if(tempMin < 0 || tempMin>maxPrice) {
                feedbackMin.setText("Min price must be >= "+defaultMinPrice+" and <= "+maxPrice+". Defaulting to "+minPrice+" - "+maxPrice+".");
                minEntry.selectAll();
                return false;
            }else {
                minPrice=tempMin;
                feedbackMin.setText("");
                return true;
            }
        }catch (NumberFormatException n){
            feedbackMin.setText("Please enter a valid number for min price. Defaulting to "+minPrice+" - "+maxPrice+".");
            minEntry.selectAll();
            return false;
        }
    }

    /**
     * The purpose of this method is to validate the user's input for max price.
     * @param maxEntry the JTextField used to enter max price.
     * @return true if valid price, false if invalid.
     */
    private boolean checkMax(JTextField maxEntry){
        feedbackMax.setText("");
        try{
            float tempMax = Float.parseFloat(maxEntry.getText());
            if(tempMax < minPrice) {
                feedbackMax.setText("Max price must be >= min price. Defaulting to "+minPrice+" - "+maxPrice+".");
                maxEntry.selectAll();
                return false;
            }else {
                maxPrice = tempMax;
                feedbackMax.setText("");
                return true;
            }
        }catch (NumberFormatException n){
            feedbackMax.setText("Please enter a valid number for max price. Defaulting to "+minPrice+" - "+maxPrice+".");
            maxEntry.selectAll();
            return false;
        }
    }

    /**
     * The purpose of this method is to create a JList scroll pane.
     * @param selectItems the JList which will be the list in the scroll pane.
     * @return a scroll pane containing a JList.
     */
    public JScrollPane generateJListScrollPane(JList<String> selectItems){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(selectItems);
        selectItems.setLayoutOrientation(JList.VERTICAL);
        scrollPane.setPreferredSize(new Dimension(250, 60));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0) ));

        return scrollPane;
    }

    /**
     * The purpose of this method is to generate a JPanel for getting brands.
     * @param instruction the text instruction to the user
     * @param scrollPane the scroll pane containing the JList
     * @return a formatted JPanel containing the instruction and scroll pane
     */
    public JPanel generateFinalScrollJPanel(String instruction, JScrollPane scrollPane){
        JLabel instructionLabel = new JLabel(instruction);
        instructionLabel.setAlignmentX(0);
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel,BoxLayout.Y_AXIS));
        itemsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        itemsPanel.add(instructionLabel);
        scrollPane.setAlignmentX(0);
        itemsPanel.add(scrollPane);
        itemsPanel.add(Box.createRigidArea(new Dimension(0,5)));

        return itemsPanel;
    }

    /**
     * The purpose of this method is to create a JPanel that displays the list of available brands for the user to select from.
     * @return a JPanel with a JList of brands for the user to select from.
     */
    public JPanel userInputBrands(){
        JList<String> selectItems = new JList<>(availableBrands.toArray(new String[0]));
        selectItems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = this.generateJListScrollPane(selectItems);
        ListSelectionListener listSelectionListener = e ->  chosenBrands = new HashSet<>(selectItems.getSelectedValuesList());
        selectItems.addListSelectionListener(listSelectionListener);

        return generateFinalScrollJPanel("Select your favourite brand(s) (optional)", scrollPane);
    }

    // USED TO GET J-PANELS WITH DROP DOWN LISTS FOR HOODIE STYLE, POCKET TYPE, NECKLINE & SLEEVE TYPE
    /**
     * The purpose of this method is to create a panel that includes a J-ComboBox.
     * @param jComboBox The JComboBox that is to be displayed on the panel.
     * @return JPanel containing the JComboBox.
     */
    public JPanel generateComboBoxPanel(JComboBox jComboBox){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setAlignmentX(0);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(jComboBox);
        panel.add(Box.createRigidArea(new Dimension(0,5)));

        return panel;
    }

    // HOODIE SPECIFIC CRITERIA
    /**
     * The purpose of this method is to create a JPanel that includes a ComboBox of HoodieStyles.
     * @return a JPanel containing a comboBox ofd hoodie styles.
     */
    public JPanel userInputHoodieStyle(){
        // Add a drop-down list of the Hoodie Styles
        JComboBox<HoodieStyle> jComboBox = new JComboBox<>(HoodieStyle.values());
        jComboBox.setAlignmentX(0);
        // Default to selecting NA
        jComboBox.setSelectedItem(HoodieStyle.NA);
        // Update the hoodieStyle variable to the user's selection, including if they change it.
        hoodieStyle = (HoodieStyle) jComboBox.getSelectedItem();
        jComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                hoodieStyle = (HoodieStyle) jComboBox.getSelectedItem();
            }
        });

        // Call the generateComboBoxPanel to return a panel with the HoodieStyle comboBox.
        return generateComboBoxPanel(jComboBox);
    }

    /**
     * The purpose of this method is to create a JPanel that includes a ComboBox of PocketTypes.
     * @return a JPanel containing a comboBox of pocket types.
     */
    public JPanel userInputPocketTypes(){
        // Add a drop-down list of the Pocket Types
        JComboBox<PocketType> jComboBox = new JComboBox<>(PocketType.values());
        jComboBox.setAlignmentX(0);
        // Default to selecting NA
        jComboBox.setSelectedItem(PocketType.NA);
        // Update the pocketType variable to the user's selection, including if they change it.
        pocketType = (PocketType) jComboBox.getSelectedItem();
        jComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pocketType = (PocketType) jComboBox.getSelectedItem();
            }
        });

        // Call the generateComboBoxPanel to return a panel with the pocketType comboBox.
        return generateComboBoxPanel(jComboBox);
    }

    // T-SHIRT SPECIFIC CRITERIA
    /**
     * The purpose of this method is to create a JPanel that includes a ComboBox of PocketTypes.
     * @return a JPanel containing a comboBox of pocket types.
     */
    public JPanel userInputNeckline(){
        // Add a drop-down list of the different necklines.
        JComboBox<Neckline> jComboBox = new JComboBox<>(Neckline.values());
        jComboBox.setAlignmentX(0);
        // Default to selecting NA
        jComboBox.setSelectedItem(Neckline.NA);
        // Update the neckline variable to the user's selection, including if they change it.
        neckline = (Neckline) jComboBox.getSelectedItem();
        jComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                neckline = (Neckline) jComboBox.getSelectedItem();
            }
        });

        // Call the generateComboBoxPanel to return a panel with the neckline comboBox.
        return generateComboBoxPanel(jComboBox);
    }

    /**
     * The purpose of this method is to create a JPanel that includes a ComboBox of PocketTypes.
     * @return a JPanel containing a comboBox of pocket types.
     */
    public JPanel userInputSleeveType(){
        // Add a drop-down list of the sleeve types
        JComboBox<SleeveType> jComboBox = new JComboBox<>(SleeveType.values());
        jComboBox.setAlignmentX(0);
        // Default to selecting NA
        jComboBox.setSelectedItem(SleeveType.NA);
        // Update the sleeveType variable to the user's selection, including if they change it.
        sleeveType = (SleeveType) jComboBox.getSelectedItem();
        jComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sleeveType = (SleeveType) jComboBox.getSelectedItem();
            }
        });

        // Call the generateComboBoxPanel to return a panel with the sleeveType comboBox.
        return generateComboBoxPanel(jComboBox);
    }

    // OUTER PANELS
    /**
     * The purpose of this method is to display a panel to the user be required for every search.
     * @return a JPanel that will always be displayed when the user begins their search regardless of the garment type.
     */
    public JPanel userInputAlwaysDisplayed(){
        JPanel filters = new JPanel();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));

        filters.add(this.userInputSize());
        filters.add(this.userInputBrands());
        filters.add(this.userInputMaterial());
        filters.add(this.userInputPriceRange());

        return filters;
    }

    /**
     * The purpose of this method is to create a label containing images of garments that's serves as a placeholder
     * card while the user selects the garment type they want to search for.
     * @return a JPanel with images that is displayed while the user selects their preferred garment type.
     */
    public JPanel imagePanel(){
        // Create image icons
        JLabel breakingBad = new JLabel(new ImageIcon("breakingBad_.jpg"));
        JLabel keepCalm = new JLabel(new ImageIcon("keepCalm_.jpg"));
        JLabel walkingDead = new JLabel(new ImageIcon("walkingDead_.jpg"));
        JLabel westWorld = new JLabel(new ImageIcon("westWorld_.jpg"));

        // Create the JPanel that displays everything to the user.
        JPanel imagePanel = new JPanel();
        imagePanel.add(breakingBad);
        imagePanel.add(keepCalm);
        imagePanel.add(walkingDead);
        imagePanel.add(westWorld);

        return imagePanel;
    }

    /**
     * The purpose of this method is to create a JPanel that contains both of the comboBox panels for Hoodie Criteria.
     * @return a JPanel with hoodie specific criteria comboBoxes.
     */
    public JPanel userInputHoodie(){
        // Create the outer panel.
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.X_AXIS));
        jPanel.setAlignmentX(0);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        // Add the first comboBox panel
        JPanel hoodieStyle = userInputHoodieStyle();
        hoodieStyle.setAlignmentX(0);
        jPanel.add(hoodieStyle);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        // Add the second comboBox panel
        JPanel pocketType = userInputPocketTypes();
        pocketType.setAlignmentX(0);
        jPanel.add(pocketType);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        return jPanel;
    }

    /**
     * The purpose of this method is to create a JPanel that contains both of the comboBox panels for T-Shirt Criteria.
     * @return a JPanel with T-Shirt specific criteria comboBoxes.
     */
    public JPanel userInputTShirt(){
        // Create the outer panel.
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.X_AXIS));
        jPanel.setAlignmentX(0);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        // Add the first comboBox panel.
        JPanel neckline = userInputNeckline();
        neckline.setAlignmentX(0);
        jPanel.add(neckline);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        // Add the second comboBox panel.
        JPanel sleeveType = userInputSleeveType();
        sleeveType.setAlignmentX(0);
        jPanel.add(sleeveType);
        jPanel.add(Box.createRigidArea(new Dimension(0,30)));

        return jPanel;
    }

    // GETTERS
    /**
     * The purpose of this getter is to return the user's garment type selection.
     * @return the user's garment type selection
     */
    public GarmentType getGarmentType(){
        return garmentType;
    }

    /**
     * The purpose of this getter is to return the users size selections.
     * @return the users size selections.
     */
    public Set<Size> getSizes(){
        return sizes;
    }

    /**
     * The purpose of this getter is to return the users material selection.
     * @return the users material selection.
     */
    public Material getMaterial(){
        return material;
    }

    /**
     * The purpose of this getter is to return the user's minimum price requirement.
     * @return an int value representing the minimum price the user wishes to spend.
     */
    public float getMinPrice(){
        return minPrice;
    }

    /**
     * The purpose of this getter is to return the user's maximum price requirement.
     * @return an int value representing the maximum price the user wishes to spend.
     */
    public float getMaxPrice(){
        return maxPrice;
    }

    /**
     * The purpose of this getter is to return a list of the user's preferred brands.
     * @return a Set of the users preferred brands from the available brands in the inventory.
     */
    public Set<String> getChosenBrands(){
        return new HashSet<>(chosenBrands);
    }

    /**
     * The purpose of this method is to return the user's preferred hoodie style.
     * @return a HoodieStyle object that represents the user's choice.
     */
    public HoodieStyle getHoodieStyle() {
        return hoodieStyle;
    }

    /**
     * The purpose of this method is to return the user's preferred pocket type.
     * @return a PocketType object that represents the user's choice.
     */
    public PocketType getPocketType() {
        return pocketType;
    }

    /**
     * The purpose of this method is to return the user's preferred neckline.
     * @return a Neckline object that represents the user's choice.
     */
    public Neckline getNeckline() {
        return neckline;
    }

    /**
     * The purpose of this method is to return the user's preferred sleeve type.
     * @return a SleeveType object that represents the user's choice.
     */
    public SleeveType getSleeveType() {
        return sleeveType;
    }
}
