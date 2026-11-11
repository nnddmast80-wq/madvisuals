package madvisuals;

public class Module {
    public final String name;
    public final int key;
    public boolean enabled;
    public Module(String name, int key, boolean enabled) {
        this.name = name; this.key = key; this.enabled = enabled;
    }
    public void toggle() { enabled = !enabled; }
}
