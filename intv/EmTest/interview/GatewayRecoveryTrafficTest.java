/**
 * Recovery: seven Claude failures then three successes → Claude UP again; gateway 100% Claude.
 * Run: {@code javac Main.java GatewayRecoveryTrafficTest.java && java GatewayRecoveryTrafficTest}
 */
public class GatewayRecoveryTrafficTest {

    private static final int X = 5;
    private static final int Y = 3;

    public static void main(String[] args) {
        testClaudeRecoveredServes100PercentAgain();
        System.out.println("GatewayRecoveryTrafficTest: all passed");
    }

    private static void testClaudeRecoveredServes100PercentAgain() {
        Tracker tracker = new Tracker();
        Model[] models = new Model[10];
        boolean[] ok = new boolean[10];
        for (int i = 0; i < 7; i++) {
            models[i] = Model.CLAUDE;
            ok[i] = false;
        }
        for (int i = 7; i < 10; i++) {
            models[i] = Model.CLAUDE;
            ok[i] = true;
        }
        seedTimeline(tracker, models, ok);

        ModelPolicyConfig claudeCfg = new ModelPolicyConfig(X, Y, 5);
        ModelPolicyConfig openaiCfg = new ModelPolicyConfig(X, Y, 5);
        PolicyProvider policy = new PolicyProvider(tracker, claudeCfg, openaiCfg);

        assertTrue(
                policy.healthForRouting(Model.CLAUDE) == ModelHealth.UP,
                "After 7F+3S, last y=3 Claude successes → UP (recovery checked before x-fail streak)");
        assertTrue(
                tracker.lastNForModelAll(Y, true, Model.CLAUDE),
                "Last 3 Claude attempts should all be successes");

        AIStrategy claude = new ClaudeStrategy();
        AIStrategy openai = new OpenAIStrategy();
        LlmGateway gateway = new LlmGateway(tracker, policy, claude, openai);
        String out = gateway.askPrompt("post-recovery");
        assertTrue(
                out.startsWith("Claude:"),
                "Recovered Claude should take 100% traffic; got: " + out);
    }

    private static void seedTimeline(Tracker tracker, Model[] models, boolean[] successes) {
        if (models.length != successes.length) {
            throw new IllegalArgumentException("models and successes length mismatch");
        }
        for (int i = 0; i < models.length; i++) {
            tracker.addRequest(new Req("hist-" + i, models[i], successes[i]));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
