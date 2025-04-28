import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

abstract class AbstractDietPlan {
    public abstract String suggestDiet(int cycleDay, String preference, int painLevel, int hungerLevel, int exhaustionLevel, Set<String> allergies, Set<String> dislikes);
}

interface DietRecommendation {
    String suggestDiet(int cycleDay, String preference, int painLevel, int hungerLevel, int exhaustionLevel, Set<String> allergies, Set<String> dislikes);
}

class CyclePhase {
    public static String getCyclePhase(int day) {
        if (day >= 1 && day <= 5) return "Menstrual Phase";
        if (day >= 6 && day <= 14) return "Follicular Phase";
        if (day >= 15 && day <= 17) return "Ovulation Phase";
        return "Luteal Phase";
    }
}

class DietPlan extends AbstractDietPlan implements DietRecommendation {
    private final List<String> constantFoods = Arrays.asList("water", "fruits", "vegetables", "whole grains");

    @Override
    public String suggestDiet(int cycleDay, String preference, int painLevel, int hungerLevel, int exhaustionLevel, Set<String> allergies, Set<String> dislikes) {
        String phase = CyclePhase.getCyclePhase(cycleDay);
        List<String> foods = new ArrayList<>(constantFoods);

        if (preference.equalsIgnoreCase("Vegetarian")) {
            addVegetarianFoods(foods, phase);
            addExtraFoods(foods, painLevel, hungerLevel, exhaustionLevel, "vegetarian_foods.csv");
        } else {
            addNonVegetarianFoods(foods, phase);
            addExtraFoods(foods, painLevel, hungerLevel, exhaustionLevel, "non_vegetarian_foods.csv");
        }

        foods.removeAll(allergies);
        foods.removeAll(dislikes);

        return "Recommended Foods: " + String.join(", ", foods);
    }

    private void addVegetarianFoods(List<String> foods, String phase) {
        switch (phase) {
            case "Menstrual Phase":
                foods.addAll(Arrays.asList("spinach", "lentils", "almonds", "tofu", "warm soups"));
                break;
            case "Follicular Phase":
                foods.addAll(Arrays.asList("protein-rich foods", "nuts", "yogurt", "avocados"));
                break;
            case "Ovulation Phase":
                foods.addAll(Arrays.asList("leafy greens", "berries", "seeds", "whole grains"));
                break;
            case "Luteal Phase":
                foods.addAll(Arrays.asList("bananas", "dark chocolate", "chickpeas"));
                break;
        }
    }

    private void addNonVegetarianFoods(List<String> foods, String phase) {
        switch (phase) {
            case "Menstrual Phase":
                foods.addAll(Arrays.asList("red meat", "eggs", "chicken", "bone broth"));
                break;
            case "Follicular Phase":
                foods.addAll(Arrays.asList("lean meats", "fish", "eggs", "dairy"));
                break;
            case "Ovulation Phase":
                foods.addAll(Arrays.asList("salmon", "tuna", "nuts", "lean protein"));
                break;
            case "Luteal Phase":
                foods.addAll(Arrays.asList("turkey", "chicken", "whole grains"));
                break;
        }
    }

    private void addExtraFoods(List<String> foods, int painLevel, int hungerLevel, int exhaustionLevel, String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 4) {
                    continue; // Skip lines with insufficient data
                }

                boolean addFood = false;

                if (painLevel == Integer.parseInt(values[0]) && painLevel != 0) {
                    addFood = true;
                }
                if (hungerLevel == Integer.parseInt(values[1]) && hungerLevel != 0) {
                    addFood = true;
                }
               if (exhaustionLevel == Integer.parseInt(values[2]) && exhaustionLevel != 0) {
                    addFood = true;
                }

                if (addFood) {
                    foods.add(values[3]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class CycleSyncApp extends JFrame implements ActionListener {
    private JTextField nameField, painField, hungerField, exhaustionField;
    private JComboBox<String> yearDropdown, monthDropdown, dayDropdown, cycleLengthDropdown, dietChoice;
    private JTextArea resultArea;
    private JButton submitButton, clearButton;
    private DietPlan dietPlan;
    private JCheckBox[] symptomCheckboxes, allergyCheckboxes, dislikeCheckboxes;

    public CycleSyncApp() {
        setTitle("Menstrual Diet Recommendation");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        dietPlan = new DietPlan();

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(14, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameField = createTextField(inputPanel, "Name:");
        createDateDropdowns(inputPanel, "Last Period Start Date:");
        cycleLengthDropdown = createCycleLengthDropdown(inputPanel, "Cycle Length (days):");
        dietChoice = createComboBox(inputPanel, "Diet Preference:", new String[]{"Vegetarian", "Non-Vegetarian"});
        painField = createTextField(inputPanel, "Pain Level (1-10):");
        hungerField = createTextField(inputPanel, "Hunger Level (1-10):");
        exhaustionField = createTextField(inputPanel, "Exhaustion Level (1-10):");
        symptomCheckboxes = createCheckboxPanel(inputPanel, "Symptoms:", new String[]{"cramps", "headache", "bloating", "mood swings", "fatigue"});
        allergyCheckboxes = createCheckboxPanel(inputPanel, "Allergies:", new String[]{"nuts", "dairy", "gluten", "soy", "seafood"});
        dislikeCheckboxes = createCheckboxPanel(inputPanel, "Disliked Foods:", new String[]{"broccoli", "brussels sprouts", "eggplant", "mushrooms", "spinach"});

        submitButton = createButton("Get Recommendations");
        clearButton = createButton("Clear");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Recommendations"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JTextField createTextField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField textField = new JTextField();
        panel.add(textField);
        return textField;
    }

    private void createDateDropdowns(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new FlowLayout());

        yearDropdown = new JComboBox<>();
        for (int year = LocalDate.now().getYear() ; year <= LocalDate.now().getYear(); year++) {
            yearDropdown.addItem(String.valueOf(year));
        }

        monthDropdown = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthDropdown.addItem(String.format("%02d", month));
        }

        dayDropdown = new JComboBox<>();
        for (int day = 1; day <= 31; day++) {
            dayDropdown.addItem(String.format("%02d", day));
        }

        datePanel.add(yearDropdown);
        datePanel.add(monthDropdown);
        datePanel.add(dayDropdown);
        panel.add(datePanel);
    }

    private JComboBox<String> createCycleLengthDropdown(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JComboBox<String> comboBox = new JComboBox<>();
        for (int i = 1; i <= 28; i++) {
            comboBox.addItem(String.valueOf(i));
        }
        panel.add(comboBox);
        return comboBox;
    }

    private JComboBox<String> createComboBox(JPanel panel, String label, String[] options) {
        panel.add(new JLabel(label));
        JComboBox<String> comboBox = new JComboBox<>(options);
        panel.add(comboBox);
        return comboBox;
    }

    private JCheckBox[] createCheckboxPanel(JPanel panel, String label, String[] options) {
        panel.add(new JLabel(label));
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(options.length, 1));
        JCheckBox[] checkboxes = new JCheckBox[options.length];
        for (int i = 0; i < options.length; i++) {
            checkboxes[i] = new JCheckBox(options[i]);
            checkboxPanel.add(checkboxes[i]);
        }
        panel.add(checkboxPanel);
        return checkboxes;
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String recommendation = getRecommendations();
            resultArea.setText(recommendation);
        } else if (e.getSource() == clearButton) {
            clearFields();
        }
    }

    private String getRecommendations() {
        int year = Integer.parseInt((String) yearDropdown.getSelectedItem());
        int month = Integer.parseInt((String) monthDropdown.getSelectedItem());
        int day = Integer.parseInt((String) dayDropdown.getSelectedItem());
        LocalDate lastPeriod = LocalDate.of(year, month, day);

        int cycleLength = Integer.parseInt((String) cycleLengthDropdown.getSelectedItem());
        Set<String> selectedAllergies = getSelectedCheckboxes(allergyCheckboxes);
        int painLevel = Integer.parseInt(painField.getText());
        int hungerLevel = Integer.parseInt(hungerField.getText());
        int exhaustionLevel = Integer.parseInt(exhaustionField.getText());
        Set<String> selectedDislikes = getSelectedCheckboxes(dislikeCheckboxes);

        int cycleDay = (int) ChronoUnit.DAYS.between(lastPeriod, LocalDate.now()) % cycleLength + 1;
        String dietPreference = (String) dietChoice.getSelectedItem();
        return dietPlan.suggestDiet(cycleDay, dietPreference, painLevel, hungerLevel, exhaustionLevel, selectedAllergies, selectedDislikes);
    }

    private Set<String> getSelectedCheckboxes(JCheckBox[] checkboxes) {
        Set<String> selectedItems = new HashSet<>();
        for (JCheckBox checkbox : checkboxes) {
            if (checkbox.isSelected()) {
                selectedItems.add(checkbox.getText());
            }
        }
        return selectedItems;
    }

    private void clearFields() {
        nameField.setText("");
        yearDropdown.setSelectedIndex(0);
        monthDropdown.setSelectedIndex(0);
        dayDropdown.setSelectedIndex(0);
        cycleLengthDropdown.setSelectedIndex(0);
        painField.setText("");
        hungerField.setText("");
        exhaustionField.setText("");
        clearCheckboxes(symptomCheckboxes);
        clearCheckboxes(allergyCheckboxes);
        clearCheckboxes(dislikeCheckboxes);
        resultArea.setText("");
    }

    private void clearCheckboxes(JCheckBox[] checkboxes) {
        for (JCheckBox checkbox : checkboxes) {
            checkbox.setSelected(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CycleSyncApp app = new CycleSyncApp();
            app.setVisible(true);
        });
    }
}
