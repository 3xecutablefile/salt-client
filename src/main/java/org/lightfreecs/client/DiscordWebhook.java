/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Discord webhook utility class
 */
public class DiscordWebhook {
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) { this.url = url; }

    public void setContent(String content) { this.content = content; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTts(boolean tts) { this.tts = tts; }

    public void addEmbed(EmbedObject embed) { this.embeds.add(embed); }

    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty()) throw new IllegalArgumentException("Set content or add embeds");
        HttpsURLConnection connection = (HttpsURLConnection) new URL(this.url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        JSONObject json = new JSONObject();
        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);
        if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList<>();
            for (EmbedObject embed : this.embeds) {
                JSONObject jsonEmbed = new JSONObject();
                jsonEmbed.put("title", embed.getTitle());
                jsonEmbed.put("description", embed.getDescription());
                jsonEmbed.put("url", embed.getUrl());
                if (embed.getColor() != null) jsonEmbed.put("color", embed.getColor().getRGB());
                if (embed.getFooter() != null) {
                    JSONObject jsonFooter = new JSONObject();
                    jsonFooter.put("text", embed.getFooter().getText());
                    jsonFooter.put("icon_url", embed.getFooter().getIconUrl());
                    jsonEmbed.put("footer", jsonFooter);
                }
                if (embed.getImage() != null) {
                    JSONObject jsonImage = new JSONObject();
                    jsonImage.put("url", embed.getImage().getUrl());
                    jsonEmbed.put("image", jsonImage);
                }
                if (embed.getThumbnail() != null) {
                    JSONObject jsonThumbnail = new JSONObject();
                    jsonThumbnail.put("url", embed.getThumbnail().getUrl());
                    jsonEmbed.put("thumbnail", jsonThumbnail);
                }
                if (embed.getAuthor() != null) {
                    JSONObject jsonAuthor = new JSONObject();
                    jsonAuthor.put("name", embed.getAuthor().getName());
                    jsonAuthor.put("url", embed.getAuthor().getUrl());
                    jsonAuthor.put("icon_url", embed.getAuthor().getIconUrl());
                    jsonEmbed.put("author", jsonAuthor);
                }
                List<JSONObject> jsonFields = new ArrayList<>();
                for (var field : embed.getFields()) {
                    JSONObject jsonField = new JSONObject();
                    jsonField.put("name", field.getName());
                    jsonField.put("value", field.getValue());
                    jsonField.put("inline", field.isInline());
                    jsonFields.add(jsonField);
                }
                jsonEmbed.put("fields", jsonFields.toArray());
                embedObjects.add(jsonEmbed);
            }
            json.put("embeds", embedObjects.toArray());
        }
        try (OutputStream os = connection.getOutputStream()) {
            os.write(json.toString().getBytes());
        }
        connection.getResponseCode();
    }

    public static class EmbedObject {
        private String title, description, url;
        private Color color;
        private Footer footer;
        private Image image;
        private Image thumbnail;
        private Author author;
        private final List<Field> fields = new ArrayList<>();

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        public Color getColor() { return color; }
        public Footer getFooter() { return footer; }
        public Image getImage() { return image; }
        public Image getThumbnail() { return thumbnail; }
        public Author getAuthor() { return author; }
        public List<Field> getFields() { return fields; }

        public EmbedObject setTitle(String title) { this.title = title; return this; }
        public EmbedObject setDescription(String description) { this.description = description; return this; }
        public EmbedObject setUrl(String url) { this.url = url; return this; }
        public EmbedObject setColor(Color color) { this.color = color; return this; }
        public EmbedObject setFooter(String text, String icon) { this.footer = new Footer(text, icon); return this; }
        public EmbedObject setImage(String url) { this.image = new Image(url); return this; }
        public EmbedObject setThumbnail(String url) { this.thumbnail = new Image(url); return this; }
        public EmbedObject setAuthor(String name, String url, String icon) { this.author = new Author(name, url, icon); return this; }
        public EmbedObject addField(String name, String value, boolean inline) { this.fields.add(new Field(name, value, inline)); return this; }
    }

    public static class Footer { public final String text, iconUrl; public Footer(String text, String iconUrl) { this.text = text; this.iconUrl = iconUrl; } public String getText() { return text; } public String getIconUrl() { return iconUrl; } }
    public static class Image { public final String url; public Image(String url) { this.url = url; } public String getUrl() { return url; } }
    public static class Author { public final String name, url, iconUrl; public Author(String name, String url, String iconUrl) { this.name = name; this.url = url; this.iconUrl = iconUrl; } public String getName() { return name; } public String getUrl() { return url; } public String getIconUrl() { return iconUrl; } }
    public static class Field { public final String name, value; public final boolean inline; public Field(String name, String value, boolean inline) { this.name = name; this.value = value; this.inline = inline; } public String getName() { return name; } public String getValue() { return value; } public boolean isInline() { return inline; } }

    static class JSONObject {
        private final Map<String, Object> map = new HashMap<>();
        void put(String key, Object value) { if (value != null) map.put(key, value); }
        @Override public String toString() {
            StringBuilder sb = new StringBuilder("{");
            Set<String> keys = map.keySet();
            int i = 0;
            for (String key : keys) {
                sb.append("\"").append(key).append("\":");
                Object value = map.get(key);
                if (value instanceof String) sb.append("\"").append(value).append("\"");
                else if (value instanceof JSONObject) sb.append(value.toString());
                else if (value.getClass().isArray()) {
                    sb.append("[");
                    int len = Array.getLength(value);
                    for (int j = 0; j < len; j++) {
                        sb.append(Array.get(value, j).toString());
                        if (j < len - 1) sb.append(",");
                    }
                    sb.append("]");
                } else sb.append(value);
                if (++i < keys.size()) sb.append(",");
            }
            return sb.append("}").toString();
        }
    }
}
