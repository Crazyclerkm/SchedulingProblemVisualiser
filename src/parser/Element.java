package parser;

import java.util.Map;
import java.util.HashMap;

public abstract class Element {
    private Map<String, String> attributes = new HashMap<String, String>();

    public void addAttribute(String name, String data) {
        this.attributes.put(name, data);
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }
}
