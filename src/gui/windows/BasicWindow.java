package gui.windows;

import javax.swing.*;
import java.util.ResourceBundle;

public abstract class BasicWindow extends JInternalFrame {
    protected BasicWindow(boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
        super(null, resizable, closable, maximizable, iconifiable);
    }

    protected abstract String getTitleKey();

    protected void setupDefaultCloseOperation() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void setTranslatedTitle(ResourceBundle bundle) {
        setTitle(bundle.getString(getTitleKey()));
    }
}