package com.obiscr.chatgpt.core.builder;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.obiscr.chatgpt.core.DataFactory;

import java.util.UUID;

/**
 * @author Wuzi
 */
public class OfficialBuilder {

    public static JSONObject build(String text) {
        JSONObject result = new JSONObject();
        result.put("action","next");
        JSONArray messages = new JSONArray();
        JSONObject message0 = new JSONObject();
        message0.put("id", UUID.randomUUID());
        message0.put("role", "user");
        JSONObject content = new JSONObject();
        content.put("content_type","text");
        JSONArray parts = new JSONArray();
        parts.add(text);
        content.put("parts",parts);
        message0.put("content", content);
        messages.add(message0);
        result.put("messages", messages);
        result.put("parent_message_id", UUID.randomUUID());
        result.put("model","text-davinci-002-render");
        String conversationId = DataFactory.getInstance().getConversationId();

        if (conversationId != null && !conversationId.isEmpty()) {
            result.put("conversation_id",conversationId);
        }
        return result;
    }

    public static JSONObject buildGpt3(String text, int maxToken) {
        JSONObject result = new JSONObject();
        result.put("model", "text-davinci-003");
        result.put("prompt", text);
        result.put("temperature", 0.7);
        result.put("max_tokens", maxToken);
        result.put("stream", false);
        result.put("top_p", 1);
        result.put("frequency_penalty", 0);
        result.put("presence_penalty", 0);
        return result;
    }

    public static JSONObject buildGpt3(String text) {
        return buildGpt3(text, 3000);
    }
}
