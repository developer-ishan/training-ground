/**
 * Healthy traffic: all Claude successes, policy UP, gateway routes 100% to Claude.
 * Run: {@code javac Main.java GatewayHealthyTrafficTest.java && java GatewayHealthyTrafficTest}
 */
public class GatewayHealthyTrafficTest {

    private static final int X = 5;
    private static final int Y = 3;

    private static final boolean[] TRAFFIC_HISTORY_ALL_CLAUDE_OK = {
            true, true, true, true, true,
            true, true, true, true, true
    };

    public static void main(String[] args) {
        testPolicyAfterHistory();
        testGatewayRoutesClaudeWhenRecovered();
        System.out.println("GatewayHealthyTrafficTest: all passed");
    }

    private static void testPolicyAfterHistory() {
        Tracker tracker = new Tracker();
        seedClaudeOnly(tracker, TRAFFIC_HISTORY_ALL_CLAUDE_OK);

        ModelPolicyConfig claudeCfg = new ModelPolicyConfig(X, Y, 5);
        ModelPolicyConfig openaiCfg = new ModelPolicyConfig(X, Y, 5);
        PolicyProvider policy = new PolicyProvider(tracker, claudeCfg, openaiCfg);

        assertTrue(
                policy.healthForRouting(Model.CLAUDE) == ModelHealth.UP,
                "Claude should be UP: last y=3 attempts all succeeded");
        assertTrue(
                policy.healthForRouting(Model.OPENAI) == ModelHealth.UNKNOWN,
                "OpenAI should be UNKNOWN: no OAI attempts in history");
        assertTrue(
                tracker.lastNForModelAll(Y, true, Model.CLAUDE),
                "Tracker should show last 3 Claude calls all success");
        assertTrue(
                !tracker.lastNForModelAll(X, false, Model.CLAUDE),
                "Claude should not satisfy x=5 all-fail window");
    }

    private static void testGatewayRoutesClaudeWhenRecovered() {
        Tracker tracker = new Tracker();
        seedClaudeOnly(tracker, TRAFFIC_HISTORY_ALL_CLAUDE_OK);

        ModelPolicyConfig claudeCfg = new ModelPolicyConfig(X, Y, 5);
        ModelPolicyConfig openaiCfg = new ModelPolicyConfig(X, Y, 5);
        PolicyProvider policy = new PolicyProvider(tracker, claudeCfg, openaiCfg);

        AIStrategy claude = new ClaudeStrategy();
        AIStrategy openai = new OpenAIStrategy();
        LlmGateway gateway = new LlmGateway(tracker, policy, claude, openai);

        String out = gateway.askPrompt("after-history");
        assertTrue(out.startsWith("Claude:"), "Expected primary Claude when not DOWN; got: " + out);
    }

    private static void seedClaudeOnly(Tracker tracker, boolean[] successes) {
        for (int i = 0; i < successes.length; i++) {
            tracker.addRequest(new Req("hist-" + i, Model.CLAUDE, successes[i]));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
