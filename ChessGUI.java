package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {

    private static final int SQUARE_SIZE = 60;
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color HIGHLIGHT_COLOR = new Color(153, 255, 153, 150);

    private char[][] board = {
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
    };

    private boolean whiteTurn = true;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<int[]> validMoves = new ArrayList<>();
    private boolean[][] pawnMoved = new boolean[8][8];
    private boolean whiteQueenExists = true;
    private boolean blackQueenExists = true;

    public ChessGUI() {
        setTitle("Java Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(8 * SQUARE_SIZE, 8 * SQUARE_SIZE + 30);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawHighlights(g);
            }
        };

        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / SQUARE_SIZE;
                int row = e.getY() / SQUARE_SIZE;

                if (selectedRow == -1) {
                    if (board[row][col] != ' ' && isCorrectTurn(board[row][col])) {
                        selectedRow = row;
                        selectedCol = col;
                        validMoves = getValidMoves(row, col);
                    }
                } else {
                    if (row == selectedRow && col == selectedCol) {
                        selectedRow = -1;
                        selectedCol = -1;
                        validMoves.clear();
                    } else {
                        boolean validClick = false;
                        for (int[] move : validMoves) {
                            if (move[0] == row && move[1] == col) {
                                movePiece(selectedRow, selectedCol, row, col);

                                if ((row == 0 || row == 7) && Character.toLowerCase(board[row][col]) == 'p') {

                                    char potentiallyCapturedKing = checkPotentialKingCapture(row, col, whiteTurn);
                                    if (potentiallyCapturedKing != ' ') {
                                        promotePawnToQueen(row, col);
                                    } else {
                                        promotePawn(row, col);
                                    }

                                } else {
                                    char capturedKing = isKingCaptured();
                                    if (capturedKing != ' ') {
                                        String winner = (capturedKing == 'k' ? "White" : "Black");
                                        JOptionPane.showMessageDialog(ChessGUI.this, winner + " wins!");
                                        resetGame();
                                    } else {
                                        whiteTurn = !whiteTurn;
                                    }
                                }

                                selectedRow = -1;
                                selectedCol = -1;
                                validMoves.clear();
                                if (Character.toLowerCase(board[row][col]) == 'p') {
                                    pawnMoved[row][col] = true;
                                }
                                validClick = true;
                                break;
                            }
                        }
                        if (!validClick) {
                            selectedRow = -1;
                            selectedCol = -1;
                            validMoves.clear();
                        }
                    }

                }
                repaint();
            }
        });
        add(boardPanel);
        setVisible(true);
    }

    private char checkPotentialKingCapture(int row, int col, boolean isWhiteTurn) {
        char originalPiece = board[row][col];
        char queenPiece = isWhiteTurn ? 'Q' : 'q';
        board[row][col] = queenPiece;

        char capturedKing = isKingCaptured();

        board[row][col] = originalPiece;

        return capturedKing;
    }

    private void promotePawnToQueen(int row, int col) {
        char queenPiece = whiteTurn ? 'Q' : 'q';
        board[row][col] = queenPiece;

        if (queenPiece == 'Q') whiteQueenExists = true;
        if (queenPiece == 'q') blackQueenExists = true;

        char capturedKing = isKingCaptured();
        if (capturedKing != ' ') {
            String winner = (capturedKing == 'k' ? "White" : "Black");
            JOptionPane.showMessageDialog(ChessGUI.this, winner + " wins!");
            resetGame();
        } else {
            whiteTurn = !whiteTurn;
        }

        repaint();
    }


    private void promotePawn(int row, int col) {
        String[] options;
        if (whiteTurn) {
            options = whiteQueenExists ? new String[]{"Rook", "Knight", "Bishop"} : new String[]{"Queen", "Rook", "Knight", "Bishop"};
        } else {
            options = blackQueenExists ? new String[]{"Rook", "Knight", "Bishop"} : new String[]{"Queen", "Rook", "Knight", "Bishop"};
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                "Promote pawn to:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        char newPiece;
        if (whiteTurn) {
            switch (choice) {
                case 0:
                    newPiece = (whiteQueenExists) ? 'R' : 'Q';
                    break;
                case 1:
                    newPiece = 'N';
                    break;
                case 2:
                    newPiece = 'B';
                    break;
                default:
                    newPiece = 'Q'; // Default
                    break;
            }
        } else {
            switch (choice) {
                case 0:
                    newPiece = (blackQueenExists) ? 'r' : 'q';
                    break;
                case 1:
                    newPiece = 'n';
                    break;
                case 2:
                    newPiece = 'b';
                    break;
                default:
                    newPiece = 'q'; // Default
                    break;
            }
        }

        board[row][col] = newPiece;
        if (newPiece == 'Q') whiteQueenExists = true;
        if (newPiece == 'q') blackQueenExists = true;

        char capturedKing = isKingCaptured();
        if (capturedKing != ' ') {
            String winner = (capturedKing == 'k' ? "White" : "Black");
            JOptionPane.showMessageDialog(ChessGUI.this, winner + " wins!");
            resetGame();
        } else {
            whiteTurn = !whiteTurn;
        }

        repaint();
    }

    private void resetGame() {
        board = new char[][]{
                {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
                {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
                {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
        };
        whiteTurn = true;
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        pawnMoved = new boolean[8][8];
        whiteQueenExists = true;
        blackQueenExists = true;
    }

    private boolean isCorrectTurn(char piece) {
        if (Character.isUpperCase(piece) && whiteTurn) return true;
        return Character.isLowerCase(piece) && !whiteTurn;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = ' ';

        if (Character.toLowerCase(piece) == 'k') {
            if (Math.abs(fromCol - toCol) == 2) {
                if (toCol > fromCol) {
                    board[toRow][toCol - 1] = board[toRow][7];
                    board[toRow][7] = ' ';
                } else {
                    board[toRow][toCol + 1] = board[toRow][0];
                    board[toRow][0] = ' ';
                }
            }
        }
    }

    private char isKingCaptured() {
        boolean whiteKingPresent = false;
        boolean blackKingPresent = false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 'k') blackKingPresent = true;
                if (board[i][j] == 'K') whiteKingPresent = true;
            }
        }
        if (!whiteKingPresent) return 'K';
        if (!blackKingPresent) return 'k';
        return ' ';
    }


    private void drawHighlights(Graphics g) {
        g.setColor(HIGHLIGHT_COLOR);
        for (int[] move : validMoves) {
            g.fillRect(move[1] * SQUARE_SIZE, move[0] * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color squareColor = (row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                g.setColor(squareColor);
                g.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);

                char piece = board[row][col];
                if (piece != ' ') {
                    drawPiece(g, piece, row, col);
                }
            }
        }
    }

    private void drawPiece(Graphics g, char piece, int row, int col) {
        String pieceStr = String.valueOf(piece);
        g.setColor(Character.isUpperCase(piece) ? Color.WHITE : Color.BLACK);
        Font font = new Font("Arial", Font.PLAIN, 40);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics(font);
        int x = col * SQUARE_SIZE + (SQUARE_SIZE - fm.stringWidth(pieceStr)) / 2;
        int y = row * SQUARE_SIZE + (SQUARE_SIZE + fm.getAscent() - fm.getDescent()) / 2;

        g.drawString(pieceStr, x, y);
    }

    private List<int[]> getValidMoves(int fromRow, int fromCol) {
        List<int[]> moves = new ArrayList<>();
        char piece = board[fromRow][fromCol];
        boolean isWhite = Character.isUpperCase(piece);

        if (piece == ' ') return moves;

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                    if (board[toRow][toCol] == ' ' || (Character.isUpperCase(board[toRow][toCol]) != isWhite)) {
                        moves.add(new int[]{toRow, toCol});
                    }
                }
            }
        }
        return moves;
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        if (piece == ' ') return false;

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);

        boolean isWhite = Character.isUpperCase(piece);

        if ((isWhite && !whiteTurn) || (!isWhite && whiteTurn)) {
            return false;
        }

        switch (Character.toLowerCase(piece)) {
            case 'p':
                int startRow = isWhite ? 6 : 1;
                int dir = isWhite ? -1 : 1;

                if (colDiff == 0 && fromRow + dir == toRow && board[toRow][toCol] == ' ') {
                    return true;
                }

                if (colDiff == 0 && fromRow == startRow && fromRow + 2 * dir == toRow && board[toRow][toCol] == ' ' && board[fromRow + dir][toCol] == ' ') {
                    return true;
                }

                if (colDiff == 1 && fromRow + dir == toRow && board[toRow][toCol] != ' ' && (Character.isUpperCase(board[toRow][toCol]) != isWhite)) {
                    if (board[toRow][toCol] == (isWhite ? 'k' : 'K')) {
                        return true;
                    }
                    return true;
                }
                break;

            case 'r':
                if (rowDiff == 0 || colDiff == 0) {
                    if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
                        return false;
                    }
                    return true;
                }
                break;

            case 'n':
                if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
                    return true;
                }
                break;

            case 'b':
                if (rowDiff == colDiff) {
                    if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
                        return false;
                    }
                    return true;
                }
                break;

            case 'q':
                if (rowDiff == 0 || colDiff == 0 || rowDiff == colDiff) {
                    if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
                        return false;
                    }
                    return true;
                }
                break;

            case 'k':
                if (rowDiff <= 1 && colDiff <= 1) {
                    return true;
                }
                break;
        }

        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);
        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != ' ') {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        return true;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
