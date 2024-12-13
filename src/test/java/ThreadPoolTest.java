
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadPoolTest {

    @BeforeAll
    public static void setup() {

    }

    @Test
    public void test_success() {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        queue.add(2);

        assertEquals(1, queue.size(), "Queue size is 1");
        queue.remove();
        assertTrue(queue.isEmpty(), "Queue is empty");

        assertThrows(NoSuchElementException.class, () -> queue.remove());
    }
}
