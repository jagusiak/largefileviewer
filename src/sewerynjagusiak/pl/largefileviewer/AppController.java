/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sewerynjagusiak.pl.largefileviewer;

import sewerynjagusiak.pl.largefileviewer.views.OpenWindow;
import sewerynjagusiak.pl.largefileviewer.views.MainWindow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import sewerynjagusiak.pl.largefileviewer.limiters.LineLimiter;

/**
 *
 * @author Seweryn
 */
public class AppController {

    private MainWindow mainView;
    private OpenWindow openView;
    private FileChunker chunker;
    
    public AppController() {
        final AppController instance = this;
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            this.showError("UI Error", e.getLocalizedMessage());
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                instance.mainView = new MainWindow();
                instance.mainView.setVisible(true);
                instance.mainView.lfvMWFileMenuOpen.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.showOpenDialog();
                    }
                });
                instance.mainView.lfvMWNextButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.nextChunk();
                    }
                });
                instance.mainView.lfvMWActionMenuNext.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.nextChunk();
                    }
                });
                instance.mainView.lfvMWReloadButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.reload();
                    }
                });
                instance.mainView.lfvMWActionMenuReload.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.reload();
                    }
                });
                instance.mainView.lfvMWFileMenuAbout.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(instance.mainView, "Large File Viewer\nversion 0.1\n\nauthor: Seweryn Jagusiak <jagusiak@gmail.com>\nhttp://sewerynjagusiak.pl", "About", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                instance.openView = new OpenWindow();
                instance.openView.setVisible(false);
                instance.openView.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        instance.closeOpenDialog();
                    }
                });
                instance.openView.lfvOFChooseFileButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.selectFile();
                    }
                });
                instance.openView.lfvOFSubmitView.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instance.viewFile();
                    }
                });
            }
        });
    }
    
    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select large file");
        fileChooser.setAcceptAllFileFilterUsed(true);
        if (fileChooser.showOpenDialog(this.openView) == JFileChooser.APPROVE_OPTION) {
            this.openView.lfvOFChooseFilePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            this.openView.lfvOFSubmitView.setEnabled(true);
        } else {
            this.clearOpenDialog();
        }
    }
    
    private void viewFile() {
        if (!this.openView.lfvOFChooseFilePath.getText().equals("")) {
            if (null != this.chunker) {
                try {
                    this.chunker.close();
                } catch (IOException e) {
                    this.showError("Close File", "Couldn't close previous file\n" + e.getLocalizedMessage());
                    this.clearOpenDialog();
                }
            }
            
            try {
                this.chunker = new FileChunker(
                    this.openView.lfvOFChooseFilePath.getText(),
                    new LineLimiter((Integer)this.openView.lfvOFChunkMethodLinesValue.getValue())
                );
            } catch (FileNotFoundException e) {
                this.showError("Open File", "Couldn't find file\n" + e.getLocalizedMessage());
                this.clearOpenDialog();
            } catch (UnsupportedEncodingException e) {
                this.showError("Open File", "Unsupported encoding\n" + e.getLocalizedMessage());
                this.clearOpenDialog();
            } catch (IOException e) {
                this.showError("Open File", "Read file issue\n" + e.getLocalizedMessage());
                this.clearOpenDialog();
            }
            
            this.nextChunk();
            this.closeOpenDialog();
        } else {
            this.showError("View error", "File is not selected!");
        }
    }
    
    private void nextChunk() {
        if (null != this.chunker) {
            if (this.chunker.isTerminated()) {
                return ;
            }
            try {
                this.mainView.lfvMWDisplay.setText(this.chunker.getNextChunk());
            } catch (IOException e) {
                this.showError("File error", "Reading file error\n" + e.getLocalizedMessage());
            }
            this.mainView.lfvMWNextButton.setEnabled(!this.chunker.isTerminated());
            this.mainView.lfvMWReloadButton.setEnabled(this.chunker.getLineCount() != this.chunker.getLineNumber());
            this.mainView.lfvMWDisplay.setCaretPosition(0);
            this.status();
        } else {
            this.showError("File Error", "File not selected yet");
        }
    }
    
    private void reload() {
        if (null != this.chunker) {
            try {
                this.chunker.reset();
                this.nextChunk();
            } catch (IOException e) {
                this.showError("File error", "Reading file error\n" + e.getLocalizedMessage());
            }
        } else {
            this.showError("File Error", "File not selected yet");
        }
    }
    
    private void clearOpenDialog() {
        this.openView.lfvOFChooseFilePath.setText("");
        this.openView.lfvOFSubmitView.setEnabled(false);
    }
    
    private void showOpenDialog() {
        this.clearOpenDialog();
        this.mainView.setEnabled(false);
        this.openView.setVisible(true);
    }
    
    private void closeOpenDialog() {
        this.mainView.setEnabled(true);
        this.mainView.setVisible(true);
    }
    
    private void status() {
        if (null == this.chunker) {
            this.mainView.lfvMWInfoLabel.setText("...");
        } else {
            int bottom = this.chunker.getLineNumber();
            int top = bottom - this.chunker.getLineCount() + 1;
            this.mainView.lfvMWInfoLabel.setText("Scope: [" + top + ":" + bottom + "]");
        }
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String args[]) {
        AppController appController = new AppController();
    }
}
