package com.obiscr.chatgpt;

import com.obiscr.chatgpt.core.DataFactory;
import com.obiscr.chatgpt.ui.MainPanel;
import org.intellij.plugins.markdown.ui.preview.jcef.MarkdownJCEFHtmlPanel;

import javax.swing.*;


/**
 * @author Wuzi
 */
public class MyToolWindow {

    private final MainPanel panel = new MainPanel();

    public MyToolWindow() {
        DataFactory.getInstance().setMainPanel(this.panel);
    }

    public JPanel getContent() {
        return panel.init();
    }

    public MarkdownJCEFHtmlPanel getContentPanel() {
        return panel.getContentPanel();
    }
}
