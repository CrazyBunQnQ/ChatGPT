package com.obiscr.chatgpt.core;

import com.intellij.openapi.application.ApplicationManager;
import com.obiscr.chatgpt.core.builder.CloudflareBuilder;
import com.obiscr.chatgpt.core.builder.OfficialBuilder;
import com.obiscr.chatgpt.message.ChatGPTBundle;
import com.obiscr.chatgpt.settings.SettingConfiguration.SettingURLType;
import com.obiscr.chatgpt.settings.SettingsState;
import com.obiscr.chatgpt.ui.MainPanel;
import com.obiscr.chatgpt.ui.notifier.MyNotifier;
import com.obiscr.chatgpt.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendAction {
    private static final Logger LOG = LoggerFactory.getLogger(SendAction.class);

    public SendAction() {
    }

    public static SendAction getInstance() {
        return (SendAction) ApplicationManager.getApplication().getService(SendAction.class);
    }

    public void doActionPerformed(String text) {
        SettingsState state = SettingsState.getInstance().getState();

        assert state != null;

        LOG.info("ChatGPT Search: {}", text);
        if (!text.isEmpty()) {
            DataFactory.getInstance().getMainPanel().getSearchTextArea().getTextArea().setText(text);
            SseParamsBuilder builder = new SseParamsBuilder();
            if (state.urlType == SettingURLType.OFFICIAL) {
                String apiKey = SettingsState.getInstance().getState().defaultApiKey;
                if (apiKey == null || apiKey.isEmpty()) {
                    MyNotifier.notifyError(DataFactory.getInstance().getProject(), ChatGPTBundle.message("notify.config.title", new Object[0]), ChatGPTBundle.message("notify.config.text", new Object[0]));
                    return;
                }

                builder.buildUrl("https://api.openai.com/v1/completions").buildToken(apiKey).buildData(OfficialBuilder.buildGpt3(text)).buildQuestion(text);
            } else {
                if (state.urlType == SettingURLType.DEFAULT) {
                    MyNotifier.notifyError(DataFactory.getInstance().getProject(), ChatGPTBundle.message("notify.unavailable.title", new Object[0]), ChatGPTBundle.message("notify.unavailable.text", new Object[0]));
                    return;
                }

                if (state.urlType == SettingURLType.CUSTOMIZE) {
                    if (state.customizeUrl == null || state.customizeUrl.isEmpty()) {
                        MyNotifier.notifyErrorWithAction(DataFactory.getInstance().getProject(), ChatGPTBundle.message("notify.config.title", new Object[0]), ChatGPTBundle.message("notify.config.text", new Object[0]));
                        return;
                    }

                    builder.buildUrl(state.customizeUrl).buildData(OfficialBuilder.build(text));
                } else if (state.urlType == SettingURLType.CLOUDFLARE) {
                    if (state.cloudFlareUrl == null || state.cloudFlareUrl.isEmpty()) {
                        MyNotifier.notifyErrorWithAction(DataFactory.getInstance().getProject(), ChatGPTBundle.message("notify.config.title", new Object[0]), ChatGPTBundle.message("notify.config.text", new Object[0]));
                        return;
                    }

                    builder.buildUrl(state.cloudFlareUrl).buildData(CloudflareBuilder.build(text));
                }
            }

            this.dispatch(builder.build());
        }
    }

    public void dispatch(SseParams params) {
        MainPanel mainPanel = DataFactory.getInstance().getMainPanel();
        mainPanel.aroundRequest(true);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> {
            try {
                HttpUtil.post(params, mainPanel, false);
            } catch (SocketTimeoutException var3) {
                var3.printStackTrace();
                MyNotifier.notifyError(DataFactory.getInstance().getProject(), ChatGPTBundle.message("notify.timeout.error.title", new Object[0]), ChatGPTBundle.message("notify.timeout.error.text", new Object[0]));
                mainPanel.aroundRequest(false);
            } catch (Exception var4) {
                var4.printStackTrace();
                mainPanel.aroundRequest(false);
                throw new RuntimeException(var4);
            }

        });
        executorService.shutdown();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }

    }
}
