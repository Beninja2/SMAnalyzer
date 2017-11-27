package main;

import main.SMAnalyzer.CommentGroup;
import main.SMAnalyzer.CommentListAnalyzer;
import main.SMAnalyzer.CommentInstance;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import main.sminterfaces.FBClient;
import main.sminterfaces.YTClient;
import main.sminterfaces.NormalizedComment;
import main.sminterfaces.RedditClient;
import main.sminterfaces.TwitterClient;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class MainUI extends JFrame {

    //UI vars
    private JPanel mainPanel;
    private JPanel chartPanel;
    private JTextArea postText;
    private ChartPanel mainChart;
    private JMenuBar menu;
    private JMenu options, file;
    private JCheckBoxMenuItem childCommentBox, blacklistIgnoreBox, saveFile;
    private JMenuItem loadFile, dictionaryAdd, exportToFile;
    public JTable outputTable;
    private JButton urlButton, pasteButton, analyzeButton, clearButton;
    private JScrollPane jsp;
    private JScrollPane scroll;
    private JFileChooser jfc;
    private GridBagConstraints layoutConstraints;
    public final int BAR_CHART = 0;
    public final int PIE_CHART = 1;
    //restFB vars
    private FBClient FBClient;
    private YTClient YTClient;
    private RedditClient RedditClient;
    private TwitterClient TwitterClient;
    private Parse parse;

    //HumanDataAnalysisProject
    private CommentListAnalyzer Analyzer;

    private JLabel urlLabel;
    private JTextField urlText;

    public MainUI() throws IOException {
        //init
        ActionHandler ah = new ActionHandler();
        jfc = new JFileChooser("savedfiles");
        urlLabel = new JLabel("Url: ");
        urlText = new JTextField(20);
        pasteButton = new JButton("Paste");
        analyzeButton = new JButton("Analyze");
        clearButton = new JButton("Clear");
        urlButton = new JButton("Url");
        outputTable = new JTable();
        jsp = new JScrollPane(outputTable);
        mainPanel = new JPanel();

        //menu init
        menu = new JMenuBar();

        file = new JMenu("File");
        file.setMnemonic('F');
        loadFile = new JMenuItem("Load File...");
        loadFile.setMnemonic('L');
        file.add(loadFile);
        exportToFile = new JMenuItem("Export to file");
        exportToFile.setEnabled(false);
        file.add(exportToFile);
        saveFile = new JCheckBoxMenuItem("Save to File");
        file.add(saveFile);
        menu.add(file);

        options = new JMenu("Options");
        options.setMnemonic('O');
        childCommentBox = new JCheckBoxMenuItem("Child Comments");
        blacklistIgnoreBox = new JCheckBoxMenuItem("Ignore BlackList");
        dictionaryAdd = new JMenuItem("Add to dictionary");
        options.add(childCommentBox);
        options.add(blacklistIgnoreBox);
        options.add(dictionaryAdd);
        menu.add(options);

        this.setLayout(new GridBagLayout());
        layoutConstraints = new GridBagConstraints();
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridy = 0;
        this.add(menu, layoutConstraints);

        FBClient = new FBClient();
        YTClient = new YTClient();
        RedditClient = new RedditClient();
//        TwitterClient = new TwitterClient();
        
        Analyzer = new CommentListAnalyzer();

        outputTable.setVisible(false);
        clearButton.setVisible(true);
        outputTable.setPreferredScrollableViewportSize(new Dimension(400, 150));

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(urlLabel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(urlText))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(pasteButton)
                        .addComponent(analyzeButton)
                        .addComponent(clearButton))
        );
        layout.linkSize(SwingConstants.HORIZONTAL, pasteButton, analyzeButton, clearButton);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(urlLabel)
                        .addComponent(urlText)
                        .addComponent(pasteButton))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(analyzeButton))
                .addComponent(clearButton)
        );
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridy = 1;
        this.add(mainPanel, layoutConstraints);
        
        chartPanel = new JPanel();
        postText = new JTextArea();
        scroll = new JScrollPane(postText);

        urlButton.addActionListener(ah);
        pasteButton.addActionListener(ah);
        analyzeButton.addActionListener(ah);
        clearButton.addActionListener(ah);
        loadFile.addActionListener(ah);
        dictionaryAdd.addActionListener(ah);
        exportToFile.addActionListener(ah);

        this.pack();
        this.setLocation(700, 300);
        this.setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("SMAnalyzer");
    }

    private class ActionHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == pasteButton) {
                Clipboard clipboard = getToolkit().getSystemClipboard();

                Transferable clipData = clipboard.getContents(this);
                String s;
                try {
                    s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
                } catch (Exception ex) {
                    s = ex.toString();
                }
                urlText.setText(s);
            } else if (e.getSource() == clearButton) {
                clearUI();
                urlText.setText("");
            } else if (e.getSource() == dictionaryAdd) {
                JFrame inputFrame = new JFrame("Add to dictionary");
                String wordToAdd = JOptionPane.showInputDialog(inputFrame, "Please type the word to be added below");
                if (wordToAdd != null) {
                    Analyzer.addToDictionary(wordToAdd);
                }
            } else if (e.getSource() == exportToFile) {
                ArrayList<CommentInstance> comments = Analyzer.getComments();
                int returnVal = jfc.showSaveDialog(MainUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileWriter fw = new FileWriter(jfc.getSelectedFile() + ".txt");
                        fw.write(comments.get(0).getMedia() + "`");
						fw.write(Analyzer.getOriginalPost() + "`");
                        for (CommentInstance c : comments) {
                            fw.write(c.getID() + "`");
                            fw.write(c.getCommentRaw() + "`");
                            fw.write(c.getCommentTime() + "`");
                            fw.write(c.getShares() + "|");
                        }
                        fw.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (e.getSource() == loadFile) {
                ArrayList<NormalizedComment> comments = parseFile();
                Boolean isBlacklistEnabled = !blacklistIgnoreBox.isSelected();
                try {
                    Analyzer.setComments(comments);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                Analyzer.analyze(isBlacklistEnabled);
                displayData();
            } else if (e.getSource() == analyzeButton) {
                Analyzer.clearArray();
                FBClient.clearArray();
                YTClient.clearArray();
                RedditClient.clearArray();
  //              TwitterClient.clearArray();
                clearUI();
                
                String urlString = urlText.getText();
                MainUI.this.mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                if (urlString.equals(null) || urlString.equals("")) {
                    JOptionPane.showMessageDialog(null, "There's nothing to analyze.\nPlease paste a url.", "Did you really hit analyze without puttng anything in?", JOptionPane.ERROR_MESSAGE);
                } else {
                    parse = new Parse();
                    HashMap<String, String> stringMap = parse.parseUrl(urlString);
                    Boolean child = childCommentBox.isSelected();
                    Boolean isBlacklistEnabled = !blacklistIgnoreBox.isSelected();
                    exportToFile.setEnabled(true);
                    loadFile.setEnabled(false);
                    Boolean file = saveFile.isSelected();

                    if (parse.getSite().equals("facebook")) {
                        if (stringMap.size() == 1) {
                            FBClient.fetchRandomPagePost(stringMap.get("Page Name"), child, file);
                        } else if (stringMap.size() == 3) {
                            FBClient.fetchSpecificPagePost(stringMap.get("Page Name"), stringMap.get("Post Id"), child, file);
                        }
                    } else if (parse.getSite().equals("youtube")) {
                        YTClient.fetchComments(stringMap.get("Page Type"), stringMap.get("Id"));
                    } else if (parse.getSite().equals("reddit")) {
                        RedditClient.fetchComments(stringMap.get("Post Id"));
                    }else if (parse.getSite().equals("twitter")) {
//                        TwitterClient.fetchComments(stringMap.get("Post Id"));
                    }
                    try {
                        if (parse.getSite().equals("facebook")) {
                            if (!FBClient.getPostArray().isEmpty()) {
                                Analyzer.setComments(FBClient.getPostArray());
                                Analyzer.setOriginalPost(FBClient.getPostArray().get(0).getMessage());
                            }
                        } else if (parse.getSite().equals("youtube")) {
                            if (!YTClient.getPostArray().isEmpty()) {
                                Analyzer.setComments(YTClient.getPostArray());
                                Analyzer.setOriginalPost(YTClient.getPostArray().get(0).getMessage());
                            }
                        } else if (parse.getSite().equals("reddit")) {
                            if (!RedditClient.getPostArray().isEmpty()) {
                                Analyzer.setComments(RedditClient.getPostArray());
                                Analyzer.setOriginalPost(RedditClient.getPostArray().get(0).getMessage());
                            }
                        } else {
                            JOptionPane.showMessageDialog(MainUI.this, "Uh....",
                                    "I don't know how you got here, but you need to leave", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                        JOptionPane.showMessageDialog(null, ex, "Something Broke", JOptionPane.ERROR_MESSAGE);
                    }
                    Analyzer.analyze(isBlacklistEnabled);
                    displayData();
                }
            } else {
                JOptionPane.showMessageDialog(MainUI.this, "Uh....",
                        "I don't know how you got here, but you need to leave", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    void displayData() {
        try {
            //get group output data
            ArrayList<CommentGroup> groups = Analyzer.groupComments();
            //format into arrays for JTable constructor
            Object[][] tableData = new Object[groups.size()][4];
            Object[] columnNames = {"Group Keyword", "Number of Comments", "", " "};
            int row = 0;
            for (CommentGroup g : groups) {
                tableData[row][0] = g.getKeyword();
                tableData[row][1] = g.getComments().size();
                tableData[row][2] = "More Info";
                tableData[row][3] = "Add to blacklist";
                row++;
            }
            // add a chart
            JFreeChart chart;
            ChartInstance chartInstance = new ChartInstance();
            int[] alignment = Analyzer.totalAlignment();
            chart = chartInstance.Chart("Total Level Of Positivity", alignment[1],alignment[0], alignment[2]);
            mainChart = new ChartPanel(chart);
            postText.setText(Analyzer.getOriginalPost());

            //postPanel.add(new JScrollPane(postText));
            postText.setLineWrap(true);
            postText.setWrapStyleWord(true);
            postText.setEditable(false);
            scroll.setPreferredSize(new Dimension(500, 100));
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 1;
            layoutConstraints.gridy = 0;
            layoutConstraints.gridheight = 4;
            this.add(chartPanel, layoutConstraints);
            
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 2;
            layoutConstraints.gridheight = 1;
            chartPanel.add(mainChart);
            this.add(scroll, layoutConstraints);
            
            //create and populate table
            outputTable = new JTable(tableData, columnNames);
            JButton infoButton = new JButton("More Info");
            JButton blacklistButton = new JButton("Add to Blacklist");
            outputTable.getColumn("").setCellRenderer(new ButtonRenderer());
            outputTable.getColumn("").setCellEditor(
                    new ButtonEditor(new JCheckBox(), groups, infoButton, this, Analyzer));
            outputTable.getColumn(" ").setCellRenderer(new ButtonRenderer());
            outputTable.getColumn(" ").setCellEditor(
                    new ButtonEditor(new JCheckBox(), groups, blacklistButton, this, Analyzer));
            jsp = new JScrollPane(outputTable);
	    jsp.setPreferredSize(new Dimension(500, 200));
            layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
            layoutConstraints.gridx = 0;
            layoutConstraints.gridy = 3;
            layoutConstraints.gridheight = 1;
            MainUI.this.add(jsp, layoutConstraints);
            MainUI.this.pack();
            MainUI.this.mainPanel.setCursor(null);
            outputTable.setVisible(true);
            MainUI.this.repaint();
            MainUI.this.setLocationRelativeTo(null);
            
        }catch (Exception ex) {
            System.out.println(ex);
            JOptionPane.showMessageDialog(null, ex, "Something Broke", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addToBlacklist(String wordToAdd) {
        try {
            Analyzer.addToBlacklist(wordToAdd);

        } catch (IOException ex) {
            Logger.getLogger(ButtonEditor.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        Analyzer.analyze(true);
        clearUI();
        displayData();
    }

    public void clearUI() {
        outputTable.setVisible(false);
        exportToFile.setEnabled(false);
        loadFile.setEnabled(true);
        MainUI.this.remove(jsp);
        MainUI.this.remove(scroll);
        postText.setText("");
        chartPanel.removeAll();
        MainUI.this.remove(chartPanel);   
        MainUI.this.repaint();
        MainUI.this.pack();
    }

    private ArrayList<NormalizedComment> parseFile() {
        ArrayList<NormalizedComment> comments = new ArrayList();
        ArrayList<String> idList = new ArrayList();
        ArrayList<String> textList = new ArrayList();
        ArrayList<String> timeList = new ArrayList();
        ArrayList<String> shareList = new ArrayList();
        int returnVal = jfc.showOpenDialog(MainUI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String contents = new String(Files.readAllBytes(Paths.get(jfc.getSelectedFile().getPath())));
                char currentChar;
                String media = "";
                String originalPost = "";
                String currentId = "";
                String currentText = "";
                String currentTime = "";
                String currentShares = "";
                int currentState = 0;
                for (int k = 0; k < contents.length(); k++) {
                    currentChar = contents.charAt(k);
                    if (currentChar == '`') {
                        if (currentState == 2) {
                            idList.add(currentId);
                            currentId = "";
                        } else if (currentState == 3) {
                            textList.add(currentText);
                            currentText = "";
                        } else if (currentState == 4) {
                            timeList.add(currentTime);
                            currentTime = "";
                        }
                        currentState++;
                    } else if (currentChar == '|') {
                        shareList.add(currentShares);
                        currentShares = "";
                        currentState = 2;
                    } else {
                        if (currentState == 0) {
                            media += currentChar;
                        } else if (currentState == 1) {
                            originalPost += currentChar;
                        } else if (currentState == 2) {
                            currentId += currentChar;
                        } else if (currentState == 3) {
                            currentText += currentChar;
                        } else if (currentState == 4) {
                            currentTime += currentChar;
                        } else if (currentState == 5) {
                            currentShares += currentChar;
                        }
                    }
                }

                for (int k = 0; k < idList.size(); k++) {
                    NormalizedComment normCom = new NormalizedComment();
                    normCom.setMedia(media);
                    normCom.setId(idList.get(k));
                    normCom.setMessage(textList.get(k));
                    normCom.setTime(timeList.get(k));
                    normCom.setShares(shareList.get(k));
                    comments.add(normCom);
                }
                Analyzer.setOriginalPost(originalPost);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return comments;
    }
	
    public static void main(String args[]) throws IOException {
       
        try {
           UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
	} catch(Exception e) { }
         new MainUI();
    }
}