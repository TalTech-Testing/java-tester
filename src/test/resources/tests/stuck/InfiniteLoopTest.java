import org.testng.annotations.Test;

public class InfiniteLoopTest {

    @Test(timeOut = 100)
    public void testHangTimeOut() {
        InfiniteLoop.hangTimeOut();
    }

    @Test
    public void testHang() {
        InfiniteLoop.hang();
    }

    @Test
    public void testHangThread() {
        InfiniteLoop.hangThread();
    }
}