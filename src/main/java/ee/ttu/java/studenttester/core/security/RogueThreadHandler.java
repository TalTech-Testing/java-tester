package ee.ttu.java.studenttester.core.security;

import org.testng.internal.thread.ThreadUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static ee.ttu.java.studenttester.core.security.SecureEnvironment.checkAccess;

public class RogueThreadHandler {

    private static final Logger LOG = Logger.getLogger(RogueThreadHandler.class.getName());

    public static Set<Thread> getRogueThreads() {
        checkAccess();
        return rogueThreads;
    }

    private static Set<Thread> rogueThreads = new HashSet<>();

    private Map<Thread, StackTraceElement[]> threadStacks = Thread.getAllStackTraces();

    @SuppressWarnings("deprecation")
    public void checkHandleRogueThreads() {
        checkAccess();
        Thread.getAllStackTraces().keySet().stream()
                // check all new threads that got created during this run
                .filter(thread -> !threadStacks.containsKey(thread))
                .forEach(thread -> {
                    LOG.warning(String.format("Found thread %s that seems to be stuck. " +
                            "Trying to kill and hoping for the best...", thread.getName()));
                    thread.setPriority(Thread.MIN_PRIORITY);
                    if (!tryForceStop(thread)) {
                        LOG.warning(String.format("Thread %s has resisted a stop attempt, spamming additional stop signals...",
                                thread.getName()));
                        for (int i = 0; i < 10; i++) {
                            thread.stop();
                        }
                        if (!tryForceStop(thread)) {
                            LOG.warning(String.format("Thread %s has resisted all stop attempts...",
                                    thread.getName()));
                            rogueThreads.add(thread);
                        }
                    }

                });
    }

    @SuppressWarnings("deprecation")
    private static boolean tryForceStop(Thread t) {
        t.stop();
        return Thread.getAllStackTraces().containsKey(t);
    }

    public RogueThreadHandler() {
        checkAccess();
    }

}
