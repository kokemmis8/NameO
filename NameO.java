import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Random;

public class NameO {
    public static void main(String[] args) {
        // Create a JFrame instance
        JFrame frame = new JFrame("NameO");

        // Set the size of the window to fit 6 cards
        frame.setSize(1200, 1000);

        // Set the default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel with GridLayout to display the 6 Bingo cards (2 columns, 3 rows)
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 10, 10));  // 3 rows, 2 columns, with some gap

        // Create a panel for displaying the letter/number call input
        JPanel inputPanel = new JPanel();
        JLabel instructionLabel = new JLabel("Enter letter/number (e.g., B7): ");
        JTextField inputField = new JTextField(5);
        JButton submitButton = new JButton("Submit");
        JButton clearButton = new JButton("Clear");
        inputPanel.add(instructionLabel);
        inputPanel.add(inputField);
        inputPanel.add(submitButton);
        inputPanel.add(clearButton);

        // Create the Bingo cards and add them to the grid
        BingoCard[] bingoCards = new BingoCard[6];
        for (int i = 0; i < 6; i++) {
            bingoCards[i] = new BingoCard();
            JPanel cardPanel = createCardPanel(bingoCards[i]);
            gridPanel.add(cardPanel);
        }

        // Create a banner to display the BINGO message
        JLabel bingoBanner = new JLabel("", SwingConstants.CENTER);
        bingoBanner.setFont(new Font("Arial", Font.BOLD, 50));
        bingoBanner.setForeground(Color.RED);
        bingoBanner.setVisible(false);  // Initially hidden

        // Set up the action for the submit button
        submitButton.addActionListener(e -> {
            String input = inputField.getText().toUpperCase().trim();
            if (input.matches("[BINGO][1-9][0-9]?")) {  // Check if the input is a valid BINGO number (e.g., B12)
                String letter = input.substring(0, 1);
                int number = Integer.parseInt(input.substring(1));

                // Highlight the called number on each card
                for (int i = 0; i < 6; i++) {
                    bingoCards[i].highlightNumber(letter, number);
                }

                // Check for a winner on each card
                boolean winnerFound = false;
                for (int i = 0; i < 6; i++) {
                    if (bingoCards[i].checkWin()) {
                        winnerFound = true;
                        break;
                    }
                }

                if (winnerFound) {
                    bingoBanner.setText("BINGO!");
                    bingoBanner.setVisible(true);
                } else {
                    bingoBanner.setVisible(false);
                }

                // Redraw the grid with the updated highlights
                gridPanel.removeAll();
                for (int i = 0; i < 6; i++) {
                    gridPanel.add(createCardPanel(bingoCards[i]));
                }
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid input! Enter a valid combination (e.g., B7, I15).");
            }
        });

        // Set up the action for the clear button
        clearButton.addActionListener(e -> {
            // Clear all highlights on the cards and regenerate new numbers
            for (int i = 0; i < 6; i++) {
                bingoCards[i].clearHighlights();
                bingoCards[i].generateCard();  // Generate new random numbers for the cards
            }
            bingoBanner.setVisible(false);  // Hide the BINGO banner

            // Redraw the grid with new numbers and no highlights
            gridPanel.removeAll();
            for (int i = 0; i < 6; i++) {
                gridPanel.add(createCardPanel(bingoCards[i]));
            }
            frame.revalidate();
            frame.repaint();
        });

        // Create the layout for the frame
        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);  // Add input panel to the top
        frame.add(gridPanel, BorderLayout.CENTER);  // Add grid panel to the center
        frame.add(bingoBanner, BorderLayout.SOUTH);  // Add the BINGO banner at the bottom

        // Make the window visible
        frame.setVisible(true);
    }

    // Helper method to create the card panel with "BINGO" header and numbers
    private static JPanel createCardPanel(BingoCard bingoCard) {
        JPanel cardPanel = new JPanel(new GridLayout(6, 5));  // 5x5 grid for Bingo numbers, plus one for headers
        String[] headers = {"B", "I", "N", "G", "O"};

        // Add "BINGO" headers
        for (String header : headers) {
            JLabel headerLabel = new JLabel(header, SwingConstants.CENTER);
            headerLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardPanel.add(headerLabel);
        }

        // Add Bingo card numbers
        int[][] card = bingoCard.getCard();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                String text = (card[row][col] == 0) ? "FREE" : String.valueOf(card[row][col]);
                JLabel cell = new JLabel(text, SwingConstants.CENTER);
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setBackground(bingoCard.isHighlighted(row, col) ? Color.YELLOW : Color.WHITE);
                cell.setOpaque(true);
                cardPanel.add(cell);
            }
        }

        return cardPanel;
    }
}

// Non-public BingoCard class
class BingoCard {
    private int[][] card;
    private boolean[][] highlighted;  // To track which numbers have been highlighted

    public BingoCard() {
        card = new int[5][5]; // Initialize the 5x5 grid
        highlighted = new boolean[5][5];  // Initialize the highlight tracking
        generateCard();
        highlighted[2][2] = true;  // The center space is always considered highlighted ("FREE" space)
    }

    public void generateCard() {
        Random random = new Random();

        // Regenerate the card's numbers (ensure unique values for each column)
        for (int col = 0; col < 5; col++) {
            HashSet<Integer> usedNumbers = new HashSet<>();
            int min = col * 15 + 1;       // Minimum number for this column
            int max = col * 15 + 15;      // Maximum number for this column

            for (int row = 0; row < 5; row++) {
                // Skip the center cell
                if (col == 2 && row == 2) {
                    card[row][col] = 0; // Use 0 or a placeholder for "FREE"
                    continue;
                }

                int number;
                do {
                    number = random.nextInt(max - min + 1) + min;
                } while (usedNumbers.contains(number)); // Ensure no duplicates

                usedNumbers.add(number);
                card[row][col] = number;
            }
        }
    }

    public int[][] getCard() {
        return card;
    }

    // Method to highlight a specific number
    public void highlightNumber(String letter, int number) {
        int col = "BINGO".indexOf(letter);  // Find the column for the given letter
        int min = col * 15 + 1;
        int max = col * 15 + 15;

        if (number >= min && number <= max) {
            // Find the row for the number within the column
            for (int row = 0; row < 5; row++) {
                if (card[row][col] == number) {
                    highlighted[row][col] = true;  // Highlight the number
                    return;
                }
            }
        }
    }

    // Check if a number is highlighted
    public boolean isHighlighted(int row, int col) {
        return highlighted[row][col];
    }

    // Clear all highlights
    public void clearHighlights() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                highlighted[row][col] = false;
            }
        }
        highlighted[2][2] = true;  // Keep the center space as always highlighted ("FREE")
    }

    // Check if there is a winning condition (horizontal, vertical, or diagonal)
    public boolean checkWin() {
        // Check rows and columns
        for (int i = 0; i < 5; i++) {
            if (checkLine(i, 0, 0, 1) || checkLine(0, i, 1, 0)) {
                return true;
            }
        }
        // Check diagonals
        if (checkLine(0, 0, 1, 1) || checkLine(0, 4, 1, -1)) {
            return true;
        }
        return false;
    }

    // Helper method to check if a line (row, column, or diagonal) is all highlighted
    private boolean checkLine(int startRow, int startCol, int rowDelta, int colDelta) {
        for (int i = 0; i < 5; i++) {
            int row = startRow + i * rowDelta;
            int col = startCol + i * colDelta;
            if (!highlighted[row][col]) {
                return false;
            }
        }
        return true;
    }
}
