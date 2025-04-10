package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.util.ResourceBundle;
import javax.swing.*;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

public class LogWindow extends JInternalFrame implements LogChangeListener, TranslatableWindow {
    private final LogWindowSource m_logSource;
    private final TextArea m_logContent;
    private final WindowCloseHandler closeHandler;

    public LogWindow(LogWindowSource logSource, ResourceBundle bundle) {
        super("", true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        closeHandler = new WindowCloseHandler(bundle);
        addInternalFrameListener(closeHandler);

        setTranslatedTitle(bundle);
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all()) {
            content.append(entry.getMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }

    @Override
    public void onLogChanged() {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void dispose() {
        m_logSource.unregisterListener(this);
        removeInternalFrameListener(closeHandler);
        super.dispose();
    }

    @Override
    public void setTranslatedTitle(ResourceBundle bundle) {
        setTitle(bundle.getString("logWindowTitle"));
    }
}
