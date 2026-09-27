package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JPanel;

public class ExecutionPanel extends JPanel {

    private File currentFile = null;
    private Scanner scanner;
    private String fileContent = "";
    private String executionResult = "";
    
    public ExecutionPanel() {
    }

    private void init() {

    }

    public void setCurrentFile(File newFile) {
        this.currentFile = newFile;
        readFile();
        
    }

    public String getFileContent() {
        return fileContent;
    }
    
    private void readFile() {
        try {
            scanner = new Scanner(currentFile);
            
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    fileContent += line;
                }
            }
            
        } catch (FileNotFoundException ex) {
            System.getLogger(ExecutionPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
