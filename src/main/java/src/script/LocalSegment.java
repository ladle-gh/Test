package src.script;

public interface LocalSegment {
    ScriptSegment child(int at);
    String capture();
    String name();
    int totalChildren();
    int production();
}
