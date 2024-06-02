package gcodetextpanelibrary; // Paket adını kendi paket adınızla değiştirin

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fahri
 */
public class GCodeTextPane extends JPanel {
    
    public JTextPane textPane;
    private JTextPane lineNumberArea;
    
    private static final Pattern GCODE_PATTERN = Pattern.compile("[GMXMYZFIJKSPTUVABCE]+[-+]?[0-9]*\\.?[0-9]*");
    private static final SimpleAttributeSet ERROR_ATTRIBUTES = new SimpleAttributeSet();

    static {
        StyleConstants.setForeground(ERROR_ATTRIBUTES, Color.RED);
    }

    
    
    public GCodeTextPane() {
         setLayout(new BorderLayout());

        textPane = new JTextPane();
        // Font ayarları
        Font font = new Font("Consolas", Font.PLAIN, 28); // Okunaklı bir font (Consolas)
        textPane.setFont(font);
        
        // Font rengi ayarı
        //setForeground(new Color(204, 204, 204)); // Açık gri renk

        // Satır numaraları alanı oluşturma ve ekleme
        lineNumberArea = new JTextPane();
        lineNumberArea.setEditable(false);
        lineNumberArea.setFont(new Font("Consolas", Font.PLAIN, 28));
        lineNumberArea.setBackground(new Color(220, 220, 220)); // Açık gri arka plan
        lineNumberArea.setForeground(Color.LIGHT_GRAY);
        
         // DocumentFilter ekleyin
        ((AbstractDocument) textPane.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
    
         textPane.getDocument().addDocumentListener(new DocumentListener() {
              private int previousLineCount = 1; 
            @Override
            public void insertUpdate(DocumentEvent e) {
                  int currentLineCount = textPane.getDocument().getDefaultRootElement().getElementCount();
        if (currentLineCount != previousLineCount) {
            updateLineNumbers();
            previousLineCount = currentLineCount;
        }
                highlightErrors();
                
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                  int currentLineCount = textPane.getDocument().getDefaultRootElement().getElementCount();
        if (currentLineCount != previousLineCount) {
            updateLineNumbers();
            previousLineCount = currentLineCount;
        }
                highlightErrors();
                
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumberArea);

        // Kaydırma çubuğu dinleyicisi ekleme
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                // JTextPane'in kaydırma konumunu alın
                Point viewPosition = scrollPane.getViewport().getViewPosition();

                // lineNumberArea'nın kaydırma konumunu aynı yapın
                lineNumberArea.scrollRectToVisible(new Rectangle(0, viewPosition.y, lineNumberArea.getWidth(), lineNumberArea.getHeight()));
            }
        });
        
        add(scrollPane, BorderLayout.CENTER); // JScrollPane'i GCodePanel'e ekleyin

        //updateLineNumbers();
        
       
    }

    public void loadFile(String fileName) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
            textPane.getDocument().insertString(textPane.getDocument().getLength(), line + "\n", null);
        }
        // İmleci ilk satırın başına konumlandırın
        textPane.setCaretPosition(0);
    } catch (IOException | BadLocationException e) {
        e.printStackTrace();
        // Hata durumunu uygun şekilde ele alın
    }
    }
    
    private void updateLineNumbers() {
    
        int lineCount = textPane.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder lineNumbersText = new StringBuilder();
        
        // Kaydırma konumunu kaydet
        Rectangle visibleRect = lineNumberArea.getVisibleRect();
        
        for (int i = 1; i <= lineCount; i++) {
            lineNumbersText.append(i).append("\n");
        }
        lineNumberArea.setText(lineNumbersText.toString());
            // Kaydırma konumunu geri yükle
        scrollRectToVisible(visibleRect);
    }

    private void highlightErrors() {
     SwingUtilities.invokeLater(() -> {
        StyledDocument doc = textPane.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

        try {
            String text = textPane.getText(0, doc.getLength());
            Matcher matcher = Pattern.compile("[GMF][0-9]+").matcher(text);
            while (matcher.find()) {
                String code = matcher.group();
                int start = matcher.start();
                int end = matcher.end();
                SimpleAttributeSet attrSet = new SimpleAttributeSet();

                if (code.startsWith("M")) {
                    StyleConstants.setForeground(attrSet, Color.RED);
                } else if (code.startsWith("F")) {
                    StyleConstants.setForeground(attrSet, Color.BLUE);
                } else {
                    // Varsayılan rengi alın, eğer null ise siyah kullanın
                    Color defaultForegroundColor = (Color) defaultStyle.getAttribute(StyleConstants.Foreground);
                    if (defaultForegroundColor == null) {
                        defaultForegroundColor = Color.BLACK;
                    }
                    StyleConstants.setForeground(attrSet, defaultForegroundColor);
                
                }

                doc.setCharacterAttributes(start, end - start, attrSet, false);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    });
    
    }

    private boolean isValidGCode(String code) {
        // G-code geçerlilik kontrolü burada yapılacak
        return code.matches("[GXYZF]+[-+]?[0-9]*\\.?[0-9]*"); 
    }
    
    class UppercaseDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        super.insertString(fb, offset, string.toUpperCase(), attr); // Büyük harfe çevir
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        super.replace(fb, offset, length, text.toUpperCase(), attrs); // Büyük harfe çevir
    }
}
 
    
}

