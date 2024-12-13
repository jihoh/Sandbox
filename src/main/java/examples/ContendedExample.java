package examples;

import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.*;

/**
 * The @Contended annotation is a JDK (Java Development Kit) annotation introduced in Java 8, which helps to address the
 * problem of false sharing in concurrent programming. False sharing occurs when two or more threads frequently access
 * independent variables that are placed adjacently in memory, causing performance degradation due to contention on
 * the same cache line.
 *
 * When the @Contended annotation is applied to a field or a class, it gives a hint to the JVM (Java Virtual Machine)
 * to add padding around the annotated field or the fields of the annotated class. This padding helps separate the
 * fields in memory, reducing the chance of cache line contention and improving the performance of concurrent access.
 */

public class ContendedExample {

    JPanel panelA = null;
    JPanel panelB = null;
    JButton button = null;

    JPanel getPanelA() {
        if(panelA == null) {
            panelA = new JPanel();
            panelA.setBackground(Color.ORANGE);
        }
        return panelA;
    }
    JPanel getPanelB() {
        if(panelB == null) {
            panelB = new JPanel();
            panelB.setBackground(Color.ORANGE);
        }
        return panelB;
    }
}
