package gui.windows;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.util.ResourceBundle;
import javax.swing.JPanel;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

public class LogWindow extends BasicWindow implements LogChangeListener {
    private final LogWindowSource logSource;
    private final TextArea logContent;

    public LogWindow(LogWindowSource logSource, ResourceBundle bundle) {
        super(true, true, true, true);
        this.logSource = logSource;
        this.logContent = new TextArea("");
        initializeUI(bundle);
        addInternalFrameListener(new WindowCloseHandler(bundle));
    }

    @Override
    protected String getTitleKey() {
        return "logWindowItem";
    }

    private void initializeUI(ResourceBundle bundle) {
        logContent.setSize(200, 500);
        logSource.registerListener(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();

        setupDefaultCloseOperation();
        setTranslatedTitle(bundle);
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all()) {
            content.append(entry.getMessage()).append("\n");
        }
        logContent.setText(content.toString());
        logContent.invalidate();
    }

    @Override
    public void onLogChanged() {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void dispose() {
        logSource.unregisterListener(this);
        super.dispose();
    }
}