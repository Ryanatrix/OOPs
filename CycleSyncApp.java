import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    @Override
    public String suggestDiet(int cycleDay, String preference, int painLevel, int hungerLevel, int exhaustionLevel, Set<String> allergies, Set<String> dislikes) {
        String phase = CyclePhase.getCyclePhase(cycleDay);
        List<String> foods = new ArrayList<>();
        
        if (preference.equalsIgnoreCase("Vegetarian")) {
            switch (phase) {
                case "Menstrual Phase": foods.addAll(Arrays.asList("spinach", "lentils", "almonds", "tofu", "warm soups")); break;
                case "Follicular Phase": foods.addAll(Arrays.asList("protein-rich foods", "nuts", "yogurt", "avocados")); break;
                case "Ovulation Phase": foods.addAll(Arrays.asList("leafy greens", "berries", "seeds", "whole grains")); break;
                case "Luteal Phase": foods.addAll(Arrays.asList("bananas", "dark chocolate", "chickpeas")); break;
            }
        } else {
            switch (phase) {
                case "Menstrual Phase": foods.addAll(Arrays.asList("red meat", "eggs", "chicken", "bone broth")); break;
                case "Follicular Phase": foods.addAll(Arrays.asList("lean meats", "fish", "eggs", "dairy")); break;
                case "Ovulation Phase": foods.addAll(Arrays.asList("salmon", "tuna", "nuts", "lean protein")); break;
                case "Luteal Phase": foods.addAll(Arrays.asList("turkey", "chicken", "whole grains")); break;
            }
        }
        foods.removeAll(allergies);
        foods.removeAll(dislikes);
        return "Recommended Foods: " + String.join(", ", foods);
    }
}

public class CycleSyncApp extends JFrame implements ActionListener {
    private JTextField nameField, dateField, cycleField, painField, hungerField, exhaustionField;
    private JComboBox<String> dietChoice;
    private JTextArea symptomsField, allergiesField, dislikesField, resultArea;
    private JButton submitButton, clearButton;
    private DietPlan dietPlan;
    
    public CycleSyncApp() {
        setTitle("Menstrual Diet Recommendation");
        setSize(500, 600);
        setLayout(new GridLayout(14, 2));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        dietPlan = new DietPlan();
        
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);
        
        add(new JLabel("Last Period Start Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        add(dateField);
        
        add(new JLabel("Cycle Length (days):"));
        cycleField = new JTextField();
        add(cycleField);
        
        add(new JLabel("Diet Preference:"));
        dietChoice = new JComboBox<>(new String[]{"Vegetarian", "Non-Vegetarian"});
        add(dietChoice);
        
        add(new JLabel("Pain Level (1-10):"));
        painField = new JTextField();
        add(painField);
        
        add(new JLabel("Hunger Level (1-10):"));
        hungerField = new JTextField();
        add(hungerField);
        
        add(new JLabel("Exhaustion Level (1-10):"));
        exhaustionField = new JTextField();
        add(exhaustionField);
        
        add(new JLabel("Symptoms (comma-separated):"));
        symptomsField = new JTextArea();
        add(symptomsField);
        
        add(new JLabel("Allergies (comma-separated):"));
        allergiesField = new JTextArea();
        add(allergiesField);
        
        add(new JLabel("Disliked Foods (comma-separated):"));
        dislikesField = new JTextArea();
        add(dislikesField);
        
        submitButton = new JButton("Get Recommendations");
        submitButton.addActionListener(this);
        add(submitButton);
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        add(clearButton);
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JLabel("Recommendations:"));
        add(new JScrollPane(resultArea));
        
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            LocalDate lastPeriod = LocalDate.parse(dateField.getText());
            int cycleLength = Integer.parseInt(cycleField.getText());
            String dietPreference = (String) dietChoice.getSelectedItem();
            int painLevel = Integer.parseInt(painField.getText());
            int hungerLevel = Integer.parseInt(hungerField.getText());
            int exhaustionLevel = Integer.parseInt(exhaustionField.getText());
            Set<String> allergies = new HashSet<>(Arrays.asList(allergiesField.getText().split(",")));
            Set<String> dislikes = new HashSet<>(Arrays.asList(dislikesField.getText().split(",")));
            
            int cycleDay = (int) ChronoUnit.DAYS.between(lastPeriod, LocalDate.now()) % cycleLength + 1;
            String recommendation = dietPlan.suggestDiet(cycleDay, dietPreference, painLevel, hungerLevel, exhaustionLevel, allergies, dislikes);
            resultArea.setText(recommendation);
        } else if (e.getSource() == clearButton) {
            nameField.setText("");
            dateField.setText("");
            cycleField.setText("");
            painField.setText("");
            hungerField.setText("");
            exhaustionField.setText("");
            symptomsField.setText("");
            allergiesField.setText("");
            dislikesField.setText("");
            resultArea.setText("");
        }
    }
    
    public static void main(String[] args) {
        new CycleSyncApp();
    }
}
