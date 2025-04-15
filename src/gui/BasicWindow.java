package gui;

import javax.swing.JInternalFrame;
import java.util.ResourceBundle;

public abstract class BasicWindow extends JInternalFrame {

    protected BasicWindow(boolean resizable,
                                        boolean closable,
                                        boolean maximizable,
                                        boolean iconifiable) {
        super("", resizable, closable, maximizable, iconifiable);
    }

    protected abstract String getTitleKey();

    public final void setTranslatedTitle(ResourceBundle bundle) {
        if (bundle == null) {
            setTitle("[" + getTitleKey() + "]");
            return;
        }

        try {
            setTitle(bundle.getString(getTitleKey()));
        } catch (Exception e) {
            setTitle("[" + getTitleKey() + "]");
        }
    }

    protected final void setupDefaultCloseOperation(ResourceBundle bundle) {
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        addInternalFrameListener(new WindowCloseHandler(bundle));
    }
}