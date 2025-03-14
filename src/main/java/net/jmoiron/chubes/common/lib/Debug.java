package net.jmoiron.chubes.common.lib;

import java.util.Arrays;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public class Debug {

    public static StackTraceElement[] callStack(int depth) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int actualDepth = Math.min(depth + 2, stackTraceElements.length); // Adjust depth to account for getStackTrace() and getCurrentCallStack() methods
        StackTraceElement[] result = new StackTraceElement[actualDepth - 2];
        System.arraycopy(stackTraceElements, 2, result, 0, actualDepth - 2);
        return result;
    }

    public static void printStack(int depth) {
        var frames = callStack(depth);

        System.out.println("Stack:");
        Arrays.stream(frames).forEach(frame -> {
            System.out.println("  > " + frame);
        });
    }

    public static String fmtWidgetData(Widget w) {
        var id = w.getId();
        var pos = w.getPosition();
        var widgetType = w.getClass().getName();
        return String.format("id=%s pos=%s widgetType=%s", id, pos.toString(), widgetType);
    }

}
