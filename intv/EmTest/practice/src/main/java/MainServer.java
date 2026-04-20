import gateway.Gateway;
import healthcheck.ConsecutiveHealthStrategy;
import healthcheck.WindowedHealthStrategy;
import llm.ClaudeProvider;
import llm.OpenAiProvider;

public final class MainServer {
    public static void main(String[] args) {
        ConsecutiveHealthStrategy consecutiveHealthStrategy = new ConsecutiveHealthStrategy(5, 3);
        WindowedHealthStrategy windowedHealthStrategy = new WindowedHealthStrategy(10, 0.5, 0.5);

        ClaudeProvider claude = new ClaudeProvider(consecutiveHealthStrategy);
        OpenAiProvider openAi = new OpenAiProvider(windowedHealthStrategy);

        Gateway gateway = new Gateway(claude, openAi);

        String prompt = args.length > 0 ? args[0] : "hello";

        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(gateway.askPrompt(prompt));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        claude.flag = false;
        System.out.println("=============================================");
        System.out.println("====================Claude is down=====================");
        System.out.println("=============================================");
        for (int i = 0; i < 1000; i++) {
            try {
                System.out.println(gateway.askPrompt(prompt));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        claude.flag = true;
        System.out.println("=============================================");
        System.out.println("====================Claude is up=====================");
        System.out.println("=============================================");
        for (int i = 0; i < 4; i++) {
            try {
                System.out.println(gateway.askPrompt(prompt));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        // both gpt and claude are down
        claude.flag = false;
        openAi.flag = false;
        System.out.println("=============================================");
        System.out.println("====================Both are down=====================");
        System.out.println("=============================================");
        for (int i = 0; i < 1000; i++) {
            try {
                System.out.println(gateway.askPrompt(prompt));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        // both gpt and claude are up
        claude.flag = true;
        openAi.flag = true;
        System.out.println("=============================================");
        System.out.println("====================Both are up=====================");
        System.out.println("=============================================");
        for (int i = 0; i < 1000; i++) {
            try {
                System.out.println(gateway.askPrompt(prompt));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

    }
}
